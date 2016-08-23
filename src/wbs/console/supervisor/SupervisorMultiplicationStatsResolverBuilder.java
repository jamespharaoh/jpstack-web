package wbs.console.supervisor;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.MultiplicationStatsResolver;
import wbs.console.reporting.StatsResolver;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorMultiplicationStatsResolverBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorMultiplicationStatsResolverBuilder {

	// dependencies

	@Inject
	Provider<MultiplicationStatsResolver> multiplicationStatsResolver;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorMultiplicationStatsResolverSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		List<SupervisorMultiplicationOperandSpec> operandSpecs =
			spec.operandSpecs ();

		MultiplicationStatsResolver multiplicationStatsResolver =
			this.multiplicationStatsResolver.get ();

		for (
			SupervisorMultiplicationOperandSpec operandSpec
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

			multiplicationStatsResolver.operands ().add (
				new MultiplicationStatsResolver.Operand ()

				.power (
					operandSpec.power ())

				.value (
					operandSpec.value ())

				.resolver (
					resolver)

			);

		}

		supervisorConfigBuilder.statsResolversByName ().put (
			name,
			multiplicationStatsResolver);

	}

}
