package wbs.console.helper.spec;

import static wbs.utils.etc.TypeUtils.dynamicCast;
import static wbs.utils.etc.TypeUtils.isInstanceOf;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("consoleHelperProviderSpecManager")
public
class ConsoleHelperProviderSpecManager {

	// singleton dependencies

	@SingletonDependency
	List <ConsoleModuleSpec> consoleModuleSpecs;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	@Getter
	Map <String, ConsoleHelperProviderSpec> specsByName;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			specsByName =
				ImmutableMap.copyOf (
					consoleModuleSpecs.stream ()

				.flatMap (
					consoleModuleSpec ->
						consoleModuleSpec.builders ().stream ())

				.filter (
					builder ->
						isInstanceOf (
							ConsoleHelperProviderSpec.class,
							builder))

				.map (
					builder ->
						dynamicCast (
							ConsoleHelperProviderSpec.class,
							builder))

				.collect (
					Collectors.toMap (
						ConsoleHelperProviderSpec::objectName,
						Function.identity ()))

			);

		}

	}

}
