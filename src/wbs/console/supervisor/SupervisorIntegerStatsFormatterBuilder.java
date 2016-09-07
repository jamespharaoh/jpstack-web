package wbs.console.supervisor;

import java.util.Map;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.IntegerStatsFormatter;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("supervisorIntegerStatsFormatterBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorIntegerStatsFormatterBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <IntegerStatsFormatter> integerStatsFormatterProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorIntegerStatsFormatterSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String targetBase =
			spec.targetBase ();

		Map<String,String> targetParams =
			spec.targetParams ();

		supervisorConfigBuilder.statsFormattersByName.put (
			name,
			integerStatsFormatterProvider.get ()

			.targetBase (
				targetBase)

			.targetParams (
				targetParams)

		);

	}

}
