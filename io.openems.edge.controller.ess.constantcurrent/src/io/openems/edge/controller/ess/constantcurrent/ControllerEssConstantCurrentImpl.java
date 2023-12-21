package io.openems.edge.controller.ess.constantcurrent;

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

import io.openems.common.exceptions.InvalidValueException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.HybridEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.PowerConstraint;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateActiveTime;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.Ess.ConstantCurrent", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class ControllerEssConstantCurrentImpl extends AbstractOpenemsComponent
		implements ControllerEssConstantCurrent, Controller, OpenemsComponent, TimedataProvider {

	private final CalculateActiveTime calculateCumulatedActiveTime = new CalculateActiveTime(this,
			ControllerEssConstantCurrent.ChannelId.CUMULATED_ACTIVE_TIME);

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private ManagedSymmetricEss ess;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private Battery battery;

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata = null;
	
	private Config config;

	public ControllerEssConstantCurrentImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ControllerEssConstantCurrent.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		if (this.applyConfig(context, config)) {
			return;
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
		var ess_config = OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "ess", config.ess_id());
		var battery_config = OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "battery", config.battery_id());
		return ess_config && battery_config;
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
				// Apply Active-Power Set-Point
				var acPower = getAcPower(this.ess, this.battery, this.config.hybridEssMode(), this.config.current());
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
	 * @param ess           	the {@link ManagedSymmetricEss}; checked for
	 *                      	{@link HybridEss}
	 * @param battery			the {@link Battery}
	 * @param hybridEssMode 	the {@link HybridEssMode}
	 * @param current			the configured current in mA
	 * @param power         	the calculated target power
	 * @return the AC power set-point
	 * @throws InvalidValueException 
	 */
	protected static int getAcPower(ManagedSymmetricEss ess, Battery battery, HybridEssMode hybridEssMode, int current) throws InvalidValueException {
		int voltage = battery.getVoltage().getOrError();
		int power = current * voltage / 1000;
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

		return 0; /* should never happen */
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}