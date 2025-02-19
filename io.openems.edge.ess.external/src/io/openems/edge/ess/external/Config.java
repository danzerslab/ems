package io.openems.edge.ess.external;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.ess.power.api.Phase;

@ObjectClassDefinition(//
		name = "Simulator Ess External", //
		description = "This include a extern simulated Energy Storage System.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ess0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus bridge.")
	String modbus_id() default "modbus0";

	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device.")
	int modbusUnitId() default 1;
	
	@AttributeDefinition(name = "Symetric mode enabled?", description = "true, if three Sunny Island are configured for master-slave symmetric mode")
	Phase phase() default Phase.ALL;
	
	@AttributeDefinition(name = "Read-Only mode", description = "Enables Read-Only mode")
	boolean readOnlyMode() default false;
	
	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "(enabled=true)";

	String webconsole_configurationFactory_nameHint() default "Simulator Ess External [{id}]";
}