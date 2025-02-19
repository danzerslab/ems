package io.openems.edge.controller.ess.constantcurrent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.battery.test.DummyBattery;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.controller.test.ControllerTest;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Relationship;
import io.openems.edge.ess.test.DummyHybridEss;
import io.openems.edge.ess.test.DummyManagedAsymmetricEss;

public class ControllerEssConstantCurrentImplTest {

	private static final String CTRL_ID = "ctrl0";
	private static final String ESS_ID = "ess0";
	private static final String BATT_ID = "battery0";

	@Test
	public void testOn() throws OpenemsException, Exception {
		final var ess = new DummyManagedAsymmetricEss(ESS_ID);
		final var battery = new DummyBattery(BATT_ID);
		new ControllerTest(new ControllerEssConstantCurrentImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("ess", ess) //
				.addReference("battery", battery) //
				.activate(MyConfig.create() //
						.setId(CTRL_ID) //
						.setEssId(ESS_ID) //
						.setBatteryId(BATT_ID) //
						.setMode(Mode.MANUAL_ON) //
						.setHybridEssMode(HybridEssMode.TARGET_DC) //
						.setCurrent(5) //
						.setPhase(Phase.ALL) //
						.setRelationship(Relationship.EQUALS) //
						.build()); //
	}

	@Test
	public void testOff() throws OpenemsException, Exception {
		new ControllerTest(new ControllerEssConstantCurrentImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("ess", new DummyManagedAsymmetricEss(ESS_ID)) //
				.addReference("battery", new DummyBattery(BATT_ID)) //
				.activate(MyConfig.create() //
						.setId(CTRL_ID) //
						.setEssId(ESS_ID) //
						.setBatteryId(BATT_ID) //
						.setMode(Mode.MANUAL_OFF) //
						.setHybridEssMode(HybridEssMode.TARGET_DC) //
						.setCurrent(5000) //
						.setPhase(Phase.ALL) //
						.setRelationship(Relationship.EQUALS) //
						.build()); //
	}

	public void testGetAcPower() throws OpenemsException, Exception {
	    var hybridEss = new DummyHybridEss(ESS_ID)
	            .withActivePower(7000)
	            .withMaxApparentPower(10000)
	            .withAllowedChargePower(-5000)
	            .withAllowedDischargePower(5000)
	            .withDcDischargePower(3000);

	    var battery = new DummyBattery(BATT_ID)
	            .withVoltage(200);

	    assertEquals(1000,
	            ControllerEssConstantCurrentImpl.getAcPower(hybridEss, battery, HybridEssMode.TARGET_AC, 5000));

	    assertEquals(5000, //Integer.valueOf(5000)
	            ControllerEssConstantCurrentImpl.getAcPower(hybridEss, battery, HybridEssMode.TARGET_DC, 5000));
	}
}
