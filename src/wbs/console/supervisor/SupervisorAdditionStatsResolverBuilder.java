package wbs.console.supervisor;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.AdditionStatsResolver;
import wbs.console.reporting.StatsResolver;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorAdditionStatsResolverBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorAdditionStatsResolverBuilder {

	@Inject
	Provider<AdditionStatsResolver> additionStatsResolver;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorAdditionStatsResolverSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		List<SupervisorAdditionOperandSpec> operandSpecs =
			spec.operandSpecs ();

		AdditionStatsResolver additionStatsResolver =
			this.additionStatsResolver.get ();

		for (
			SupervisorAdditionOperandSpec operandSpec
				: operandSpecs
		) {

			StatsResolver resolver = null;

			if (operandSpec.resolverName () != null) {

				resolver =
					supervisorConfigBuilder.statsResolversByName ().get (
						operandSpec.resolverName ());

				if (resolver == null) {

					throw new RuntimeException (
						stringFormat (
							"Stats resolver %s does not exist",
							operandSpec.resolverName ()));

				}

			}

			additionStatsResolver.operands ().add (
				new AdditionStatsResolver.Operand ()

				.coefficient (
					operandSpec.coefficient ())

				.resolver (
					resolver));

		}

		supervisorConfigBuilder.statsResolversByName ().put (
			name,
			additionStatsResolver);

	}

}
