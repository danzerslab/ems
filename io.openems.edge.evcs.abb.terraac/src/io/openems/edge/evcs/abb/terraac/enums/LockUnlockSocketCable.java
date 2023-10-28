package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum LockUnlockSocketCable implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	UNLOCK(0, "Unlock the cable / connector"), //
	LOCK(1, "Lock the cable / connector"), //
	;

	private final int value;
	private final String name;

	private LockUnlockSocketCable(int value, String name) {
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
