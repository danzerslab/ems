package io.openems.edge.evcs.abb.terraac.enums;

import io.openems.common.types.OptionsEnum;

public enum EvseErrorCode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	NO_ERROR(0, "No error"), //
	RESIDUAL_CURRENT(2, "Error Code 0x0002: There is a residual current in the charge circuit."),//
	PE_MISSING(4, "Error Code 0x0004: The EVSE is not earthed correctly or neutral and phase wires are swapped."),//
	OVER_VOLTAGE(8, "Error Code 0x0008: Over Voltage"), //
	UNDER_VOLTAGE(16, "Error Code 0x0010: Under Voltage"), //	
	OVER_CURRENT_FALIURE(32, "Error Code 0x0020: There is an overload on the EV side."), //	
	SEVERE_OVER_CURRENT(64, "Error Code 0x0040: There is an severe overload on the EV side."),//
	OVER_TEMPERATURE(128, "Error Code 0x0080: The internal temperature is too high."),//
	POWER_RELAY_FAULT(1024, "Error Code 0x0400: The relay contact is detected in wrong state or has damage."),//
	INTERNAL_COMMUNICATION_FAILURE(2048, "Error Code 0x0800: The internal boards of the EVSE fail to communicate with each other."),//
	E_LOCK_FAILURE(4096, "Error Code 0x1000: Error to lock / unlock the charge connector."),//
	MISSING_PHASE(8192, "Error Code 0x2000: One or more phases are missing."),//
	MODBUS_COMMUNICATION_LOST(16384, "Error Code 0x4000: The modbus communication is lost.")//
	;

	private final int value;
	private final String name;

	private EvseErrorCode(int value, String name) {
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
