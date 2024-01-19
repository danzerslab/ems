package io.openems.edge.controller.ess.controllbyexternal;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.base.GenericJsonrpcResponseSuccess;
import io.openems.common.jsonrpc.base.JsonrpcRequest;
import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;
import io.openems.common.jsonrpc.request.SetGridConnScheduleRequest;
import io.openems.common.jsonrpc.request.SetGridConnScheduleRequest.GridConnSchedule;
import io.openems.common.session.Role;
import io.openems.common.utils.JsonUtils;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.jsonapi.JsonApi;
import io.openems.edge.common.user.User;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.HybridEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.PowerConstraint;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateActiveTime;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.Ess.ControllbyExternal", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class ControllerEssControllbyExternalImpl extends AbstractOpenemsComponent
		implements ControllerEssControllbyExternal, Controller, OpenemsComponent, TimedataProvider, JsonApi {

	private final CalculateActiveTime calculateCumulatedActiveTime = new CalculateActiveTime(this,
			ControllerEssControllbyExternal.ChannelId.CUMULATED_ACTIVE_TIME);
	
	private final Logger log = LoggerFactory.getLogger(ControllerEssControllbyExternalImpl.class);


	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private ManagedSymmetricEss ess;

	private Config config;

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata = null;
	
	private List<GridConnSchedule> schedule = new CopyOnWriteArrayList<>();
	@Reference
	private ComponentManager componentManager;

	public ControllerEssControllbyExternalImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ControllerEssControllbyExternal.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		if (this.applyConfig(context, config)) {
			return;
		}
		// parse Schedule
		try {
			if (!config.schedule().trim().isEmpty()) {
				var scheduleElement = JsonUtils.parse(config.schedule());
				var scheduleArray = JsonUtils.getAsJsonArray(scheduleElement);
				this.applySchedule(scheduleArray);
			}
			this._setScheduleParseFailed(false);

		} catch (OpenemsNamedException e) {
			this._setScheduleParseFailed(true);
			this.logError(this.log, "Unable to parse Schedule: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Modified
	private void modified(ComponentContext context, Config config) {
		super.modified(context, config.id(), config.alias(), config.enabled());
		if (this.applyConfig(context, config)) {
			return;
		}
	}

	private boolean applyConfig(ComponentContext context, Config config) {
		this.config = config;
		return OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "ess", config.ess_id());
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		var isActive = false;
		try {
			isActive = switch (this.config.mode()) {
			case MANUAL_ON -> {
				
				var schedulePowerOpt = this.getEssConnSetPoint();
				this._setEssActivePowerSetPoint(schedulePowerOpt.orElse(null));
				if (!schedulePowerOpt.isPresent()) {
					this._setNoActiveSetpoint(true);
				}
				
				int schedulePower =  schedulePowerOpt.get();
				
				// Apply Active-Power Set-Point
				var acPower = getAcPower(this.ess, this.config.hybridEssMode(), schedulePower);
				PowerConstraint.apply(this.ess, this.id(), //
						this.config.phase(), Pwr.ACTIVE, this.config.relationship(), acPower);
				yield true; // is active
			}

			case MANUAL_OFF -> {
				// Do nothing
				yield false; // is not active
			}
			};

		} finally {
			this.calculateCumulatedActiveTime.update(isActive);
		}
	}

	/**
	 * Gets the required AC power set-point for AC- or Hybrid-ESS.
	 * 
	 * @param ess           the {@link ManagedSymmetricEss}; checked for
	 *                      {@link HybridEss}
	 * @param hybridEssMode the {@link HybridEssMode}
	 * @param power         the configured target power
	 * @return the AC power set-point
	 */
	protected static Integer getAcPower(ManagedSymmetricEss ess, HybridEssMode hybridEssMode, int power) {
		switch (hybridEssMode) {
		case TARGET_AC:
			return power;

		case TARGET_DC:
			if (ess instanceof HybridEss) {
				var pv = ess.getActivePower().orElse(0) - ((HybridEss) ess).getDcDischargePower().orElse(0);
				return pv + power; // Charge or Discharge
			} else {
				return power;
			}
		}

		return null; /* should never happen */
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
	
	@Override
	public CompletableFuture<JsonrpcResponseSuccess> handleJsonrpcRequest(User user, JsonrpcRequest request)
			throws OpenemsNamedException {
		user.assertRoleIsAtLeast("handleJsonrpcRequest", Role.OWNER);

		switch (request.getMethod()) {

		case SetGridConnScheduleRequest.METHOD:
			return this.handleSetEssConnScheduleRequest(user, SetGridConnScheduleRequest.from(request));

		default:
			throw OpenemsError.JSONRPC_UNHANDLED_METHOD.exception(request.getMethod());
		}
	}

	/**
	 * Handles a SetGridConnScheduleRequest.
	 *
	 * @param user    the User
	 * @param request the SetGridConnScheduleRequest
	 * @return the Future JSON-RPC Response
	 * @throws OpenemsNamedException on error
	 */
	private CompletableFuture<JsonrpcResponseSuccess> handleSetEssConnScheduleRequest(User user,
			SetGridConnScheduleRequest request) {
		this.schedule = request.getSchedule();
		return CompletableFuture.completedFuture(new GenericJsonrpcResponseSuccess(request.getId(), new JsonObject()));
	}

	/**
	 * Parses the Schedule and applies it to this Controller.
	 *
	 * @param j the {@link JsonArray} with the Schedule
	 * @throws OpenemsNamedException on error
	 */
	private void applySchedule(JsonArray j) throws OpenemsNamedException {
		this.schedule = SetGridConnScheduleRequest.GridConnSchedule.from(j);
	}
	
	/**
	 * Gets the currently valid EssConnSetPoint.
	 *
	 * @return the current setpoint.
	 */
	private Optional<Integer> getEssConnSetPoint() {
		// Is the Ess Active-Power Set-Point currently overwritten using the channel?
		var setPointFromChannel = this.getEssActivePowerSetPointChannel().getNextWriteValueAndReset();
		if (setPointFromChannel.isPresent()) {
			// Yes -> use the channel value
			return setPointFromChannel;
		}
		// No -> use the value from the Schedule
		var now = ZonedDateTime.now(this.componentManager.getClock()).toEpochSecond();
		for (GridConnSchedule e : this.schedule) {
			if (now >= e.getStartTimestamp() && now <= e.getStartTimestamp() + e.getDuration()) {
				// -> this entry is valid!
				return Optional.ofNullable(e.getActivePowerSetPoint());
			}
		}
		// Still no -> no value available
		return Optional.empty();
	}
}