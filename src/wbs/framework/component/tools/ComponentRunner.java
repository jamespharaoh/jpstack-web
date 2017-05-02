package wbs.framework.component.tools;

import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringSplitComma;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggedErrorsException;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ComponentRunner {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ComponentRunner.class);

	@Getter @Setter
	String primaryProjectName;

	@Getter @Setter
	String primaryProjectPackageName;

	@Getter @Setter
	List<String> layerNames;

	@Getter @Setter
	List<String> configNames;

	@Getter @Setter
	String runnerName;

	@Getter @Setter
	String methodName;

	@Getter @Setter
	List <String> runnerArgs;

	Class <?> runnerClass;

	public
	void run (
			@NonNull TaskLogger parentTaskLogger)
		throws Exception {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"run");

		) {

			runnerClass =
				Class.forName (
					runnerName);

			try (

				ComponentManager componentManager =
					initComponentManager (
						taskLogger);

			) {

				taskLogger.makeException ();

				Thread shutdownHookThread =
					new Thread (
						componentManager::close);

				Runtime.getRuntime ().addShutdownHook (
					shutdownHookThread);

				invokeTarget (
					taskLogger,
					componentManager);

				taskLogger.makeException ();

				Runtime.getRuntime ().removeShutdownHook (
					shutdownHookThread);

			}

		}

	}

	ComponentManager initComponentManager (
			@NonNull TaskLogger parentTaskLogger)
		throws Exception {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initComponentManager");

		) {

			return new ComponentManagerBuilder ()

				.primaryProjectName (
					primaryProjectName)

				.primaryProjectPackageName (
					primaryProjectPackageName)

				.layerNames (
					layerNames)

				.configNames (
					configNames)

				.registerComponentDefinition (
					new ComponentDefinition ()

					.componentClass (
						runnerClass)

					.name (
						uncapitalise (
							runnerClass.getSimpleName ()))

					.scope (
						"singleton"))

				//.outputPath (
				//	"work/runner/components")

				.build (
					taskLogger);

		}

	}

	public
	void invokeTarget (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentManager componentManager)
		throws Exception {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"invokeTarget");

		) {

			// find runnable and run it

			Object runner =
				componentManager.getComponentRequired (
					taskLogger,
					uncapitalise (
						runnerClass.getSimpleName ()),
					runnerClass);

			Method runMethod =
				runnerClass.getMethod (
					methodName,
					TaskLogger.class,
					List.class);

			runMethod.invoke (
				runner,
				taskLogger,
				(Object) runnerArgs);

		}

	}

	public static
	void main (
			@NonNull String[] argumentsArray) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"main");

		) {

			List <String> arguments =
				Arrays.asList (
					argumentsArray);

			if (arguments.size () < 5) {

				taskLogger.errorFormat (
					"Expects five or more parameters: %s",
					joinWithCommaAndSpace (
						"primary project name",
						"primary project package name",
						"layer names (comma separated)",
						"config names (comma separated)",
						"runner class name",
						"runner method name",
						"runner arguments..."));

				System.exit (1);

			}

			try {

				new ComponentRunner ()

					.primaryProjectName (
						arguments.get (0))

					.primaryProjectPackageName (
						arguments.get (1))

					.layerNames (
						stringSplitComma (
							arguments.get (2)))

					.configNames (
						stringSplitComma (
							arguments.get (3)))

					.runnerName (
						arguments.get (4))

					.methodName (
						arguments.get (5))

					.runnerArgs (
						arguments.subList (6, arguments.size ()))

					.run (
						taskLogger);

			} catch (LoggedErrorsException loggedErrorsException) {

				taskLogger.fatalFormat (
					"Aborting due to logged errors");

				System.exit (1);

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Failed to run component");

				System.exit (1);

			}

		}

	}

}
