package io.openems.edge.controller.ess.controllbyexternal;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.common.utils.ConfigUtils;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Relationship;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String ess_id;
		private Mode mode;
		private HybridEssMode hybridEssMode;
		private Phase phase;
		private Relationship relationship;
		private String schedule;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setEssId(String ess_id) {
			this.ess_id = ess_id;
			return this;
		}

		public Builder setMode(Mode mode) {
			this.mode = mode;
			return this;
		}

		public Builder setHybridEssMode(HybridEssMode hybridEssMode) {
			this.hybridEssMode = hybridEssMode;
			return this;
		}

		public Builder setPhase(Phase phase) {
			this.phase = phase;
			return this;
		}

		public Builder setRelationship(Relationship relationship) {
			this.relationship = relationship;
			return this;
		}
		
		public Builder setSchedule(String schedule) {
			this.schedule = schedule;
			return this;
		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 *
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String ess_id() {
		return this.builder.ess_id;
	}
	
	@Override
	public String ess_target() {
		return ConfigUtils.generateReferenceTargetFilter(this.id(), this.ess_id());
	}
	

	@Override
	public Mode mode() {
		return this.builder.mode;
	}

	@Override
	public HybridEssMode hybridEssMode() {
		return this.builder.hybridEssMode;
	}

	@Override
	public Relationship relationship() {
		return this.builder.relationship;
	}

	@Override
	public Phase phase() {
		return this.builder.phase;
	}
	
	@Override
	public String schedule() {
		return this.builder.schedule;
	}

}