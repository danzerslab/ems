package io.openems.edge.ess.external;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_3;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.channel.EnumWriteChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.ManagedAsymmetricEss;
import io.openems.edge.ess.api.ManagedSinglePhaseEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SinglePhase;
import io.openems.edge.ess.api.SinglePhaseEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.ess.external.enums.PowerSupplyStatus;
import io.openems.edge.ess.external.enums.SetControlMode;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Simulator.Ess.External", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class SimulatorEssExternalImpl extends AbstractOpenemsModbusComponent
		implements ManagedSinglePhaseEss, SinglePhaseEss, ManagedAsymmetricEss, AsymmetricEss, ManagedSymmetricEss,
		SymmetricEss, ModbusComponent, OpenemsComponent {

	@Reference
	private Power power;

	@Reference
	private ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config;
	private SinglePhase singlePhase = null;

	public SimulatorEssExternalImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				SymmetricEss.ChannelId.values(), //
				ManagedSymmetricEss.ChannelId.values(), //
				AsymmetricEss.ChannelId.values(), //
				ManagedAsymmetricEss.ChannelId.values(), //
				SinglePhaseEss.ChannelId.values(), //
				ManagedSinglePhaseEss.ChannelId.values(), //
				SimulatorEssExternal.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		this.config = config;

		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
		
		// Evaluate 'SinglePhase'
		switch (config.phase()) {
		case ALL:
			this.singlePhase = null;
			break;
		case L1:
			this.singlePhase = SinglePhase.L1;
			break;
		case L2:
			this.singlePhase = SinglePhase.L2;
			break;
		case L3:
			this.singlePhase = SinglePhase.L3;
			break;
		}

		if (this.singlePhase != null) {
			SinglePhaseEss.initializeCopyPhaseChannel(this, this.singlePhase);
		}

		this._setGridMode(GridMode.ON_GRID);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		if (this.config.readOnlyMode()) {
			return;
		}

		EnumWriteChannel setControlMode = this.channel(SimulatorEssExternal.ChannelId.SET_CONTROL_MODE);
		IntegerWriteChannel setActivePowerChannel = this.channel(SimulatorEssExternal.ChannelId.SET_ACTIVE_POWER);
		IntegerWriteChannel setReactivePowerChannel = this.channel(SimulatorEssExternal.ChannelId.SET_REACTIVE_POWER);

		setControlMode.setNextWriteValue(SetControlMode.START);
		setActivePowerChannel.setNextWriteValue(activePower);
		setReactivePowerChannel.setNextWriteValue(reactivePower);
	}

	@Override
	public void applyPower(int activePowerL1, int reactivePowerL1, int activePowerL2, int reactivePowerL2,
			int activePowerL3, int reactivePowerL3) throws OpenemsNamedException {
		if (this.config.phase() == Phase.ALL) {
			return;
		}

		ManagedSinglePhaseEss.super.applyPower(activePowerL1, reactivePowerL1, activePowerL2, reactivePowerL2,
				activePowerL3, reactivePowerL3);
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
		return new ModbusProtocol(this, //
				new FC3ReadRegistersTask(1, Priority.LOW, //
						m(SymmetricEss.ChannelId.MAX_APPARENT_POWER, new UnsignedDoublewordElement(1)), //
						m(SymmetricEss.ChannelId.ACTIVE_CHARGE_ENERGY, new UnsignedDoublewordElement(3)), //
						m(SymmetricEss.ChannelId.ACTIVE_DISCHARGE_ENERGY, new UnsignedDoublewordElement(5)), //
						m(SymmetricEss.ChannelId.CAPACITY, new UnsignedDoublewordElement(7))), //

				new FC3ReadRegistersTask(10, Priority.HIGH, //
						m(SymmetricEss.ChannelId.ACTIVE_POWER, new SignedDoublewordElement(10)), //
						m(AsymmetricEss.ChannelId.ACTIVE_POWER_L1, new SignedDoublewordElement(12)), //
						m(AsymmetricEss.ChannelId.ACTIVE_POWER_L2, new SignedDoublewordElement(14)), //
						m(AsymmetricEss.ChannelId.ACTIVE_POWER_L3, new SignedDoublewordElement(16)), //
						new DummyRegisterElement(18,19), //
						m(SimulatorEssExternal.ChannelId.GRID_VOLTAGE_L1, new SignedDoublewordElement(20),SCALE_FACTOR_MINUS_2), //
						m(SimulatorEssExternal.ChannelId.GRID_VOLTAGE_L2, new SignedDoublewordElement(22),SCALE_FACTOR_MINUS_2), //
						m(SimulatorEssExternal.ChannelId.GRID_VOLTAGE_L3, new SignedDoublewordElement(24),SCALE_FACTOR_MINUS_2), //
						new DummyRegisterElement(26, 27), //
						m(SimulatorEssExternal.ChannelId.FREQUENCY, new UnsignedDoublewordElement(28)), //
						m(AsymmetricEss.ChannelId.REACTIVE_POWER_L1, new SignedDoublewordElement(30)), //
						m(AsymmetricEss.ChannelId.REACTIVE_POWER_L2, new SignedDoublewordElement(32)), //
						m(AsymmetricEss.ChannelId.REACTIVE_POWER_L3, new SignedDoublewordElement(34))), //

				new FC3ReadRegistersTask(40, Priority.LOW, //
						m(SimulatorEssExternal.ChannelId.OPERATING_MODE_FOR_ACTIVE_POWER_LIMITATION, new UnsignedDoublewordElement(40))), //

				new FC3ReadRegistersTask(50, Priority.HIGH, //
						m(SimulatorEssExternal.ChannelId.BATTERY_CURRENT, new SignedDoublewordElement(50), SCALE_FACTOR_MINUS_3),
						m(SymmetricEss.ChannelId.SOC, new UnsignedDoublewordElement(52)), //
						m(SimulatorEssExternal.ChannelId.CURRENT_BATTERY_CAPACITY, new SignedDoublewordElement(54)), //
						m(SimulatorEssExternal.ChannelId.BATTERY_TEMPERATURE, new SignedDoublewordElement(56), SCALE_FACTOR_MINUS_1), //
						m(SimulatorEssExternal.ChannelId.BATTERY_VOLTAGE, new UnsignedDoublewordElement(58))), //

				new FC3ReadRegistersTask(60, Priority.LOW,
						m(SimulatorEssExternal.ChannelId.POWER_SUPPLY_STATUS, new UnsignedDoublewordElement(60),
								// set values at SymmetricEss.ChannelId.GRID_MODE as well
								new ElementToChannelConverter((value) -> {
									if (value == null) {
										return null;
									}

									int intValue = TypeUtils.getAsType(OpenemsType.INTEGER, value);
									final GridMode gridMode;
									if (intValue == PowerSupplyStatus.OFF.getValue()) {
										gridMode = GridMode.OFF_GRID;

									} else {
										if (intValue == PowerSupplyStatus.UTILITY_GRID_CONNECTED.getValue()) {
											gridMode = GridMode.ON_GRID;

										} else {
											gridMode = GridMode.UNDEFINED;
										}
									}
									this._setGridMode(gridMode);

									return intValue;
								}))),

				new FC3ReadRegistersTask(70, Priority.LOW,
						m(SimulatorEssExternal.ChannelId.LOWEST_MEASURED_BATTERY_TEMPERATURE, new SignedDoublewordElement(70), SCALE_FACTOR_MINUS_1),
						m(SimulatorEssExternal.ChannelId.HIGHEST_MEASURED_BATTERY_TEMPERATURE, new SignedDoublewordElement(72), SCALE_FACTOR_MINUS_1),
						m(SimulatorEssExternal.ChannelId.MAX_OCCURRED_BATTERY_VOLTAGE, new SignedDoublewordElement(74), SCALE_FACTOR_MINUS_2)),

				new FC3ReadRegistersTask(80, Priority.HIGH, //
						m(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, new UnsignedDoublewordElement(80), INVERT), //
						m(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, new UnsignedDoublewordElement(82))), //

				new FC16WriteRegistersTask(100, //
						m(SimulatorEssExternal.ChannelId.SET_ACTIVE_POWER, new SignedDoublewordElement(100)), //
						m(SimulatorEssExternal.ChannelId.SET_CONTROL_MODE, new UnsignedDoublewordElement(102)), //
						m(SimulatorEssExternal.ChannelId.SET_REACTIVE_POWER, new SignedDoublewordElement(104))), //

				new FC16WriteRegistersTask(110,
						m(SimulatorEssExternal.ChannelId.MIN_SOC_POWER_ON, new UnsignedDoublewordElement(110)), //
						m(SimulatorEssExternal.ChannelId.MIN_SOC_POWER_OFF, new UnsignedDoublewordElement(112))));
	}


	@Override
	public String debugLog() {
		return "SoC:" + this.getSoc().asString() //
				+ "|L:" + this.getActivePower().asString() //
				+ "|Allowed:" + this.getAllowedChargePower().asStringWithoutUnit() + ";" //
				+ this.getAllowedDischargePower().asString() //
				+ "|" + this.getGridModeChannel().value().asOptionString();
	}

	@Override
	public Power getPower() {
		return this.power;
	}

	@Override
	public int getPowerPrecision() {
		return 1;
	}

	@Override
	public SinglePhase getPhase() {
		return this.singlePhase;
	}

}

