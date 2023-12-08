package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum ChargePointState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	//IDLE(0, "Idle"), //
	NO_PERMISSION_BELOW_RATED_CURRENT(33536, "Vehicle attached, no permission (preparing)"), //
	NO_PERMISSION(512, "Vehicle attached, no permission"), //
	IDLE_BELOW_RATED_CURRENT(33024, "IDLE"), //
	IDLE(1280, "IDLE, EV Plug in, EVSE ready for charging"), //
	CHARGING_BELOW_RATED_CURRENT(34048, "Charging below rated current"), //
	CHARGING_WITH_NOT_ALL_PHASES(1024, "EV Plug in, EVSE ready for charging, Only on or two phases EVSE"), //
	//ERROR(5, "Charging error"), //
	CHARGING_WITH_NOT_ALL_PHASES_PWM(33792, "EV Plug in, EVSE ready for charging, Only on or two phases EVSE_PWM"), //
	NO_VEHICLE_ATTACHED(32768, "No Vehicle attached"), //
	CHARGING_FINISHED(33280, "Charging is finished") //
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
