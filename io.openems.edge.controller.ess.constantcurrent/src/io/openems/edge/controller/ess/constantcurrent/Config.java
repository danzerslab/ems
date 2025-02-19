package io.openems.edge.controller.ess.constantcurrent;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Relationship;

@ObjectClassDefinition(//
		name = "Controller Ess Constant Current", //
		description = "Defines a charge/discharge power based on a defined fix current to a symmetric energy storage system.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlEssConstantCurrent0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Mode", description = "Set the type of mode.")
	Mode mode() default Mode.MANUAL_ON;

	@AttributeDefinition(name = "Hybrid-ESS Mode", description = "For Hybrid-ESS (ESS with attached DC-side PV system): apply target power to AC or DC side of inverter?")
	HybridEssMode hybridEssMode() default HybridEssMode.TARGET_DC;

	@AttributeDefinition(name = "Ess-ID", description = "ID of Ess device.")
	String ess_id();
	
	@AttributeDefinition(name = "Battery-ID", description = "ID of Battery device.")
	String battery_id();

	@AttributeDefinition(name = "Charge/Discharge Current [mA]", description = "Negative values for Charge; positive for Discharge")
	int current();

	@AttributeDefinition(name = "Power Relationship", description = "Target power must be equal, less-than or greater-than the configured power value")
	Relationship relationship() default Relationship.EQUALS;

	@AttributeDefinition(name = "Phase", description = "Apply target power to L1, L2, L3 or sum of all phases")
	Phase phase() default Phase.ALL;

	@AttributeDefinition(name = "Ess target filter", description = "This is auto-generated by 'Ess-ID'.")
	String ess_target() default "(enabled=true)";
	
	@AttributeDefinition(name = "Battery target filter", description = "This is auto-generated by 'Battery-ID'.")
	String battery_target() default "(enabled=true)";

	String webconsole_configurationFactory_nameHint() default "Controller Ess Constant Current [{id}]";
}