package wbs.platform.supervisor;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.reporting.console.IntegerStatsFormatter;

@PrototypeComponent ("supervisorIntegerStatsFormatterBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorIntegerStatsFormatterBuilder {

	@Inject
	Provider<IntegerStatsFormatter> integerStatsFormatter;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorIntegerStatsFormatterSpec supervisorIntegerStatsFormatterSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			supervisorIntegerStatsFormatterSpec.name ();

		String targetBase =
			supervisorIntegerStatsFormatterSpec.targetBase ();

		String targetGroupParamName =
			supervisorIntegerStatsFormatterSpec.targetGroupParamName ();

		String targetStepParamName =
			supervisorIntegerStatsFormatterSpec.targetStepParamName ();

		Map<String,String> targetParams =
			supervisorIntegerStatsFormatterSpec.targetParams ();

		supervisorPageBuilder.statsFormattersByName.put (
			name,
			integerStatsFormatter.get ()
				.targetBase (targetBase)
				.targetGroupParamName (targetGroupParamName)
				.targetStepParamName (targetStepParamName)
				.targetParams (targetParams));

	}

}
