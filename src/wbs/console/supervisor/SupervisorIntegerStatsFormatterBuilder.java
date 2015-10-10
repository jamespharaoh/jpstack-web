package wbs.console.supervisor;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.IntegerStatsFormatter;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorIntegerStatsFormatterBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorIntegerStatsFormatterBuilder {

	@Inject
	Provider<IntegerStatsFormatter> integerStatsFormatter;

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

		String targetGroupParamName =
			spec.targetGroupParamName ();

		String targetStepParamName =
			spec.targetStepParamName ();

		Map<String,String> targetParams =
			spec.targetParams ();

		supervisorConfigBuilder.statsFormattersByName.put (
			name,
			integerStatsFormatter.get ()
				.targetBase (targetBase)
				.targetGroupParamName (targetGroupParamName)
				.targetStepParamName (targetStepParamName)
				.targetParams (targetParams));

	}

}
