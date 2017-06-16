package wbs.console.supervisor;

import static wbs.utils.collection.IterableUtils.iterableFilterByClass;
import static wbs.utils.collection.IterableUtils.iterableFindExactlyOneRequired;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.reporting.StatsAggregator;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsProvider;
import wbs.console.reporting.StatsResolver;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorConfigBuilder")
public
class SupervisorConfigBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	SupervisorConfigSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// properties

	@Getter @Setter
	Map <String, StatsProvider> statsProvidersByName =
		new LinkedHashMap<> ();

	@Getter @Setter
	Map <String, StatsAggregator> statsAggregatorsByName =
		new LinkedHashMap<> ();

	@Getter @Setter
	Map <String, StatsFormatter> statsFormattersByName =
		new LinkedHashMap<> ();

	@Getter @Setter
	Map <String, StatsGrouper> statsGroupersByName =
		new LinkedHashMap<> ();

	@Getter @Setter
	Map <String, StatsResolver> statsResolversByName =
		new LinkedHashMap<> ();

	@Getter @Setter
	List <StatsPagePartFactory> pagePartFactories =
		new ArrayList<> ();

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			List <Object> children =
				new ArrayList<> ();

			if (
				isNotNull (
					spec.templateName ())
			) {

				SupervisorConfigSpec templateSpec =
					iterableFindExactlyOneRequired (
						iterableFilterByClass (
							container.consoleModule ().builders (),
							SupervisorConfigSpec.class),
						otherSpec ->
							stringEqualSafe (
								spec.templateName (),
								otherSpec.name ()));

				children.addAll (
					templateSpec.builders ());

			}

			children.addAll (
				spec.builders ());

			builder.descend (
				taskLogger,
				spec,
				children,
				this,
				MissingBuilderBehaviour.ignore);

			consoleModule.addSupervisorConfig (
				new SupervisorConfig ()

				.name (
					spec.name ())

				.label (
					spec.label ())

				.spec (
					spec)

				.pagePartFactories (
					pagePartFactories)

			);

		}

	}

}
