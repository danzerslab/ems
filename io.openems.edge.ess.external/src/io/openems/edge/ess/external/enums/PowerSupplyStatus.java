package io.openems.edge.ess.external.enums;

import io.openems.common.types.OptionsEnum;

public enum PowerSupplyStatus implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	OFF(0, "Off"), //
	UTILITY_GRID_CONNECTED(1, "Utility Grid Connected"), //
	BACKUP_NOT_AVAILABLE(2, "Backup Not Available"), //
	BACKUP(3, "Backup"); //

	private final int value;
	private final String name;

	private PowerSupplyStatus(int value, String name) {
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