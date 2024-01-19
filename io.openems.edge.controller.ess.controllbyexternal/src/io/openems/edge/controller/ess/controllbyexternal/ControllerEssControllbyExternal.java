package io.openems.edge.controller.ess.controllbyexternal;

import static io.openems.common.channel.PersistencePriority.HIGH;
import static io.openems.common.channel.Unit.CUMULATED_SECONDS;
import static io.openems.common.types.OpenemsType.LONG;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.jsonapi.JsonApi;
import io.openems.edge.controller.api.Controller;

public interface ControllerEssControllbyExternal extends Controller, OpenemsComponent, JsonApi {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		CUMULATED_ACTIVE_TIME(Doc.of(LONG)//
				.unit(CUMULATED_SECONDS) //
				.persistencePriority(HIGH)),
		
		NO_ACTIVE_SETPOINT(Doc.of(Level.INFO) //
				.text("No active Set-Point given")), //
		SCHEDULE_PARSE_FAILED(Doc.of(Level.FAULT) //
				.text("Unable to parse Schedule")), //
		
		ESS_ACTIVE_POWER_SET_POINT(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_WRITE) //
				.text("Target Active-Power Setpoint at the ess inverter"));

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
	 * Gets the Channel for {@link ChannelId#NO_ACTIVE_SETPOINT}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getNoActiveSetpointChannel() {
		return this.channel(ChannelId.NO_ACTIVE_SETPOINT);
	}

	/**
	 * Gets the Run-Failed State. See {@link ChannelId#NO_ACTIVE_SETPOINT}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getNoActiveSetpoint() {
		return this.getNoActiveSetpointChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#NO_ACTIVE_SETPOINT} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setNoActiveSetpoint(boolean value) {
		this.getNoActiveSetpointChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#SCHEDULE_PARSE_FAILED}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getScheduleParseFailedChannel() {
		return this.channel(ChannelId.SCHEDULE_PARSE_FAILED);
	}

	/**
	 * Gets the Run-Failed State. See {@link ChannelId#SCHEDULE_PARSE_FAILED}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getScheduleParseFailed() {
		return this.getScheduleParseFailedChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#SCHEDULE_PARSE_FAILED} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setScheduleParseFailed(boolean value) {
		this.getScheduleParseFailedChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#ESS_ACTIVE_POWER_SET_POINT}.
	 *
	 * @return the Channel
	 */
	public default IntegerWriteChannel getEssActivePowerSetPointChannel() {
		return this.channel(ChannelId.ESS_ACTIVE_POWER_SET_POINT);
	}

	/**
	 * Gets the Active Power Limit in [W]. See
	 * {@link ChannelId#ESS_ACTIVE_POWER_SET_POINT}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getEssActivePowerSetPoint() {
		return this.getEssActivePowerSetPointChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#ESS_ACTIVE_POWER_SET_POINT} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setEssActivePowerSetPoint(Integer value) {
		this.getEssActivePowerSetPointChannel().setNextValue(value);
	}

	/**
	 * Sets the Active Power Limit in [W]. See
	 * {@link ChannelId#ESS_ACTIVE_POWER_SET_POINT}.
	 *
	 * @param value the active power limit
	 * @throws OpenemsNamedException on error
	 */
	public default void setEssActivePowerSetPoint(Integer value) throws OpenemsNamedException {
		this.getEssActivePowerSetPointChannel().setNextWriteValue(value);
	}
}
