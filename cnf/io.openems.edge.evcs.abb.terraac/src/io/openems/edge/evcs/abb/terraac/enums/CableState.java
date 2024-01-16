package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum CableState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	NO_CABLE_ATTACHED(0, "No cable attached"), //
	CABLE_ATTACHED_UNLOCKED(1, "Cable attached but unlocked in charging station."), //
	CABLE_ATTACHED_LOCKED(17, "Cable attached and locked in charging station."),//
	CABLE_ATTACHED_WITH_CAR_UNLOCKED(257, "Cable attached, car attached but unlocked in charging station."), //
	CABLE_ATTACHED_WITH_CAR_LOCKED(273, "Cable attached, car attached and locked in charging station.")//
	;

	private final int value;
	private final String name;

	private CableState(int value, String name) {
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
