package io.openems.edge.evcs.abb.terraac;

import java.util.function.Consumer;

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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.evcs.api.ChargeStateHandler;
import io.openems.edge.evcs.api.ChargingType;
import io.openems.edge.evcs.api.Evcs;
import io.openems.edge.evcs.api.EvcsPower;
import io.openems.edge.evcs.api.ManagedEvcs;
import io.openems.edge.evcs.api.Phases;
import io.openems.edge.evcs.api.Status;
import io.openems.edge.evcs.api.WriteHandler;
import io.openems.edge.evcs.abb.terraac.enums.ChargePointState;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Evcs.ABB.TerraAC", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE, //
})
public class EvcsABBTerraACImpl extends AbstractOpenemsModbusComponent
		implements EvcsABBTerraAC, Evcs, ManagedEvcs, ModbusComponent, OpenemsComponent, EventHandler {

	private static final int DETECT_PHASE_ACTIVITY = 100; // mA

	private final Logger log = LoggerFactory.getLogger(EvcsABBTerraAC.class);

	/** Handles charge states. */
	private final ChargeStateHandler chargeStateHandler = new ChargeStateHandler(this);
	/** Processes the controller's writes to this evcs component. */
	private final WriteHandler writeHandler = new WriteHandler(this);

	@Reference
	private EvcsPower evcsPower;

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config = null;

	public EvcsABBTerraACImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				Evcs.ChannelId.values(), //
				ManagedEvcs.ChannelId.values(), //
				EvcsABBTerraAC.ChannelId.values());
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		this.config = config;
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}

		/*
		 * Calculates the maximum and minimum hardware power dynamically by listening on
		 * the fixed hardware limit and the phases used for charging
		 */
		Evcs.addCalculatePowerLimitListeners(this);

		this.applyConfig(context, config);

		this.getModbusCommunicationFailedChannel()
				.onSetNextValue(t -> this._setChargingstationCommunicationFailed(t.orElse(false)));
	}

	@Modified
	private void modified(ComponentContext context, Config config) throws OpenemsNamedException {
		if (super.modified(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
		this.applyConfig(context, config);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	private void applyConfig(ComponentContext context, Config config) {
		this.config = config;
		this._setChargingType(ChargingType.AC);
		this._setFixedMinimumHardwarePower(config.minHwCurrent() / 1000 * 3 * 230);
		this._setFixedMaximumHardwarePower(config.maxHwCurrent() / 1000 * 3 * 230);
		this._setPowerPrecision(230);
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
		var modbusProtocol = new ModbusProtocol(this, //
				new FC3ReadRegistersTask(16391, Priority.LOW,
						m(EvcsABBTerraAC.ChannelId.EV_CHARGE_POWER_LIMIT, new UnsignedDoublewordElement(16391)),
						m(EvcsABBTerraAC.ChannelId.EVSE_ERROR_CODE, new UnsignedDoublewordElement(16393)),
						m(EvcsABBTerraAC.ChannelId.CABLE_STATE, new UnsignedDoublewordElement(16395)),
						m(EvcsABBTerraAC.ChannelId.CHARGE_POINT_STATE, new UnsignedDoublewordElement(16397)),						
						m(EvcsABBTerraAC.ChannelId.CHARGING_CURRENT_LIMIT, new UnsignedDoublewordElement(16399)),
						m(EvcsABBTerraAC.ChannelId.CURRENT_L1, new UnsignedDoublewordElement(16401)),
						m(EvcsABBTerraAC.ChannelId.CURRENT_L2, new UnsignedDoublewordElement(16403)),
						m(EvcsABBTerraAC.ChannelId.CURRENT_L3, new UnsignedDoublewordElement(16405)),
						m(EvcsABBTerraAC.ChannelId.VOLTAGE_L1, new UnsignedDoublewordElement(16407)),
						m(EvcsABBTerraAC.ChannelId.VOLTAGE_L2, new UnsignedDoublewordElement(16409)),
						m(EvcsABBTerraAC.ChannelId.VOLTAGE_L3, new UnsignedDoublewordElement(16411)),
						m(Evcs.ChannelId.CHARGE_POWER, new UnsignedDoublewordElement(16413)),
						m(Evcs.ChannelId.ACTIVE_CONSUMPTION_ENERGY, new UnsignedDoublewordElement(16415)),
						m(EvcsABBTerraAC.ChannelId.COM_TIMEOUT, new UnsignedWordElement(16417))),
				new FC16WriteRegistersTask(16641,
						m(EvcsABBTerraAC.ChannelId.SET_CHARGE_CURRENT, new UnsignedDoublewordElement(16641)),
						m(EvcsABBTerraAC.ChannelId.LOCK_UNLOCK_SOCKET_CABLE, new UnsignedWordElement(16643)),
						new DummyRegisterElement(16644), 
						m(EvcsABBTerraAC.ChannelId.START_CANCEL_CHARGING_SESSION, new UnsignedWordElement(16645)),
						new DummyRegisterElement(16646), 
						m(EvcsABBTerraAC.ChannelId.COM_TIMEOUT, new UnsignedWordElement(16647)))
		);
		this.addStatusListener();
		this.addPhasesListener();
		return modbusProtocol;
	}

	private void addStatusListener() {
		this.channel(EvcsABBTerraAC.ChannelId.CHARGE_POINT_STATE).onSetNextValue(s -> {
			ChargePointState state = s.asEnum();
			/**
			 * Maps the raw state into a {@link Status}.
			 */
			switch (state) {
			case CHARGING:
				this._setStatus(Status.CHARGING);
				break;
			case NO_PERMISSION:
			case CHARGING_STATION_RESERVED:
				this._setStatus(Status.CHARGING_REJECTED);
				break;
			case ERROR:
				this._setStatus(Status.ERROR);
				break;
			case NO_VEHICLE_ATTACHED:
				this._setStatus(Status.NOT_READY_FOR_CHARGING);
				break;
			case CHARGING_PAUSED:
				this._setStatus(Status.CHARGING_FINISHED);
				break;
			case CHARGING_FINISHED:
				this._setStatus(Status.CHARGING_FINISHED);
				break;
			case UNDEFINED:
			default:
				this._setStatus(Status.UNDEFINED);
			}
		});
	}

	private void addPhasesListener() {
		final Consumer<Value<Integer>> setPhases = ignore -> {
			var phases = 0;
			if (this.getCurrentL1().orElse(0) > DETECT_PHASE_ACTIVITY) {
				phases++;
			}
			if (this.getCurrentL2().orElse(0) > DETECT_PHASE_ACTIVITY) {
				phases++;
			}
			if (this.getCurrentL3().orElse(0) > DETECT_PHASE_ACTIVITY) {
				phases++;
			}
			if (phases == 0) {
				phases = 3;
			}
			this._setPhases(phases);
		};
		this.getCurrentL1Channel().onUpdate(setPhases);
		this.getCurrentL2Channel().onUpdate(setPhases);
		this.getCurrentL3Channel().onUpdate(setPhases);
	}

	@Override
	public String debugLog() {
		return "Limit:" + this.getSetChargePowerLimit().orElse(null) + "|" + this.getStatus().getName();
	}

	@Override
	public EvcsPower getEvcsPower() {
		return this.evcsPower;
	}

	@Override
	public int getConfiguredMinimumHardwarePower() {
		return Math.round(this.config.minHwCurrent() / 1000f) * DEFAULT_VOLTAGE * Phases.THREE_PHASE.getValue();
	}

	@Override
	public int getConfiguredMaximumHardwarePower() {
		return Math.round(this.config.maxHwCurrent() / 1000f) * DEFAULT_VOLTAGE * Phases.THREE_PHASE.getValue();
	}

	@Override
	public boolean getConfiguredDebugMode() {
		return this.config.debugMode();
	}
	
	@Override
	public boolean applyChargePowerLimit(int power) throws Exception {

		var phases = this.getPhasesAsInt();
		var current = Math.round((power * 1000) / phases / 230f);
		if (current < 6000) {
			current = 0;
		}
		this.setSetChargeCurrentLimit(current);
		return true;
	}

	@Override
	public boolean pauseChargeProcess() throws Exception {
		this.applyChargePowerLimit(0);
		return true;
	}

	@Override
	public boolean applyDisplayText(String text) throws OpenemsException {
		return false;
	}

	@Override
	public int getMinimumTimeTillChargingLimitTaken() {
		return 30;
	}

	@Override
	public ChargeStateHandler getChargeStateHandler() {
		return this.chargeStateHandler;
	}

	@Override
	public void logDebug(String message) {
		if (this.config.debugMode()) {
			this.logInfo(this.log, message);
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
			this.writeHandler.run();
			break;
		}
	}
}
