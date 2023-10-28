package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum ChargePointState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	IDLE(0, "Idle"), //
	NO_PERMISSION(1, "Vehicle attached, no permission (preparing)"), //
	CHARGING(1024, "Charging at rated current"), //
	CHARGING_BELOW_RATED_CURRENT(41, "Charging below rated current"), //
	CHARGING_PAUSED(3, "EV Ready for charge, S2 closed"), //
	ERROR(5, "Charging error"), //
	CHARGING_STATION_RESERVED(2, "EV Plug in, EVSE ready for charging"), //
	NO_VEHICLE_ATTACHED(456, "No Vehicle attached"), //
	CHARGING_FINISHED(3456, "Charging is finished") //
	;

	private final int value;
	private final String name;

	private ChargePointState(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}

}
