package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum StartCancelChargingSession implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	START_CHARGING_SESSION(0, "Start charging session"), //
	CANCEL_CHARGING_SESSION(1, "Cancel charging session ") //
	;

	private final int value;
	private final String name;

	private StartCancelChargingSession(int value, String name) {
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
