package io.openems.edge.controller.ess.controllbyexternal;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.Test;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.test.TimeLeapClock;
import io.openems.common.types.ChannelAddress;
import io.openems.common.utils.JsonUtils;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.controller.test.ControllerTest;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Relationship;
import io.openems.edge.ess.test.DummyHybridEss;
import io.openems.edge.ess.test.DummyManagedAsymmetricEss;

public class ControllerEssControllbyExternalImplTest {

	private static final String CTRL_ID = "ctrl0";
	private static final String ESS_ID = "ess0";

	private static final ChannelAddress CTRL_NO_ACTIVE_SETPOINT = new ChannelAddress(CTRL_ID, "NoActiveSetpoint");
	private static final ChannelAddress ESS_ACTIVE_POWER = new ChannelAddress(ESS_ID, "ActivePower");

	@Test
	public void test() throws Exception {
		final var start = 1577836800L;
		final var clock = new TimeLeapClock(Instant.ofEpochSecond(start) /* starts at 1. January 2020 00:00:00 */,
				ZoneOffset.UTC);
		new ControllerTest(new ControllerEssControllbyExternalImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("componentManager", new DummyComponentManager(clock)) //
				.addReference("ess", new DummyManagedAsymmetricEss(ESS_ID)) //
				.activate(MyConfig.create() //
						.setId(CTRL_ID) //
						.setEssId(ESS_ID) //
						.setMode(Mode.MANUAL_ON) //
						.setHybridEssMode(HybridEssMode.TARGET_DC) //
						.setPhase(Phase.ALL) //
						.setRelationship(Relationship.EQUALS) //
						.setSchedule(JsonUtils.buildJsonArray()//
								.add(JsonUtils.buildJsonObject()//
										.addProperty("startTimestamp", start + 500) //
										.addProperty("duration", 900) //
										.addProperty("activePowerSetPoint", 0) //
										.build()) //
								.add(JsonUtils.buildJsonObject()//
										.addProperty("startTimestamp", start + 500 + 800) //
										.addProperty("duration", 900) //
										.addProperty("activePowerSetPoint", 3000) //
										.build() //
								).build().toString() //
						).build()) //
				.next(new TestCase("No active setpoint") //
						.input(ESS_ACTIVE_POWER, 1000) //
						.output(CTRL_NO_ACTIVE_SETPOINT, true)) //
		;
	}
	@Test
	public void testGetAcPower() throws OpenemsException, Exception {
		var hybridEss = new DummyHybridEss(ESS_ID) //
				.withActivePower(7000) //
				.withMaxApparentPower(10000) //
				.withAllowedChargePower(-5000) //
				.withAllowedDischargePower(5000) //
				.withDcDischargePower(3000); //

		assertEquals(Integer.valueOf(5000), //
				ControllerEssControllbyExternalImpl.getAcPower(hybridEss, HybridEssMode.TARGET_AC, 5000));

		assertEquals(Integer.valueOf(9000), //
				ControllerEssControllbyExternalImpl.getAcPower(hybridEss, HybridEssMode.TARGET_DC, 5000));
	}
}