package io.openems.edge.evcs.abb.terraac;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.evcs.abb.terraac.enums.CableState;
import io.openems.edge.evcs.abb.terraac.enums.ChargePointState;
import io.openems.edge.evcs.abb.terraac.enums.EvseErrorCode;
import io.openems.edge.evcs.abb.terraac.enums.LockUnlockSocketCable;
import io.openems.edge.evcs.abb.terraac.enums.StartCancelChargingSession;

public interface EvcsABBTerraAC extends OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		CABLE_STATE(Doc.of(CableState.values())
				.accessMode(AccessMode.READ_ONLY)), //
		
		CHARGE_POINT_STATE(Doc.of(ChargePointState.values())
				.accessMode(AccessMode.READ_ONLY)), //

		EVSE_ERROR_CODE(Doc.of(EvseErrorCode.values())
				.accessMode(AccessMode.READ_ONLY)), //

		LOCK_UNLOCK_SOCKET_CABLE(Doc.of(LockUnlockSocketCable.values())
				.accessMode(AccessMode.WRITE_ONLY)), //

		START_CANCEL_CHARGING_SESSION(Doc.of(StartCancelChargingSession.values())//
				.accessMode(AccessMode.WRITE_ONLY)), //
		
		EV_CHARGE_CURRENT_LIMIT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.accessMode(AccessMode.READ_ONLY)), //
		
		CHARGING_CURRENT_LIMIT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.accessMode(AccessMode.READ_ONLY)), //

		CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.accessMode(AccessMode.READ_ONLY)), //

		CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.accessMode(AccessMode.READ_ONLY)), //

		CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)
				.accessMode(AccessMode.READ_ONLY)), //
		
		VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)
				.accessMode(AccessMode.READ_ONLY)), //
		
		VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)
				.accessMode(AccessMode.READ_ONLY)), //
		
		VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)
				.accessMode(AccessMode.READ_ONLY)), //

		COM_TIMEOUT(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.SECONDS)//
				.accessMode(AccessMode.READ_ONLY)), //

		SET_CHARGE_CURRENT(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.MILLIAMPERE)//
				.accessMode(AccessMode.WRITE_ONLY)), //
		
		COM_TIMEOUT_SET(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.SECONDS)//
				.accessMode(AccessMode.WRITE_ONLY)), //
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}
	
	/**
	 * Gets the Channel for {@link ChannelId#CURRENT_L1}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getCurrentL1Channel() {
		return this.channel(ChannelId.CURRENT_L1);
	}

	/**
	 * Gets the Current on phase 1 in [mA]. See {@link ChannelId#CURRENT_L1}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getCurrentL1() {
		return this.getCurrentL1Channel().value();
	}
	
	/**
	 * Gets the Channel for {@link ChannelId#CURRENT_L2}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getCurrentL2Channel() {
		return this.channel(ChannelId.CURRENT_L2);
	}

	/**
	 * Gets the Current on phase 2 in [mA]. See {@link ChannelId#CURRENT_L2}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getCurrentL2() {
		return this.getCurrentL2Channel().value();
	}
	
	/**
	 * Gets the Channel for {@link ChannelId#CURRENT_L3}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getCurrentL3Channel() {
		return this.channel(ChannelId.CURRENT_L3);
	}

	/**
	 * Gets the Current on phase 3 in [mA]. See {@link ChannelId#CURRENT_L3}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getCurrentL3() {
		return this.getCurrentL3Channel().value();
	}
	
	/**
	 * Gets the Channel for {@link ChannelId#SET_CHARGE_CURRENT_LIMIT}.
	 *
	 * @return the Channel
	 */
	public default IntegerWriteChannel getSetChargeCurrentLimitChannel() {
		return this.channel(ChannelId.SET_CHARGE_CURRENT);
	}

	/**
	 * Gets the SetChargeCurrentLimit. See
	 * {@link ChannelId#SET_CHARGE_CURRENT_LIMIT}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getSetChargeCurrentLimit() {
		return this.getSetChargeCurrentLimitChannel().value();
	}

	/**
	 * Sets the SetChargeCurrentLimit. See
	 * {@link ChannelId#SET_CHARGE_CURRENT_LIMIT}.
	 * 
	 * @param value {@link Integer}.
	 * @throws OpenemsNamedException on error.
	 */
	public default void setSetChargeCurrentLimit(Integer value) throws OpenemsNamedException {
		this.getSetChargeCurrentLimitChannel().setNextWriteValue(value);
	}
}
