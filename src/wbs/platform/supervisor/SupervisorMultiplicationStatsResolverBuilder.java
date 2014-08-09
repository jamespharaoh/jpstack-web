package wbs.platform.supervisor;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.reporting.console.MultiplicationStatsResolver;
import wbs.platform.reporting.console.StatsResolver;

@PrototypeComponent ("supervisorMultiplicationStatsResolverBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorMultiplicationStatsResolverBuilder {

	@Inject
	Provider<MultiplicationStatsResolver> multiplicationStatsResolver;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorMultiplicationStatsResolverSpec supervisorMultiplicationStatsResolverSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			supervisorMultiplicationStatsResolverSpec.name ();

		List<SupervisorMultiplicationOperandSpec> operandSpecs =
			supervisorMultiplicationStatsResolverSpec.operandSpecs ();

		MultiplicationStatsResolver multiplicationStatsResolver =
			this.multiplicationStatsResolver.get ();

		for (SupervisorMultiplicationOperandSpec operandSpec
				: operandSpecs) {

			StatsResolver resolver = null;

			if (operandSpec.resolverName () != null) {

				resolver =
					supervisorPageBuilder.statsResolversByName ().get (
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
					.power (operandSpec.power ())
					.value (operandSpec.value ())
					.resolver (resolver));

		}

		supervisorPageBuilder.statsResolversByName ().put (
			name,
			multiplicationStatsResolver);

	}

}
