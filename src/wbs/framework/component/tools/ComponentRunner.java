package wbs.framework.component.tools;

import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringSplitComma;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.BootstrapComponentManager;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.Log4jLogTargetFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggedErrorsException;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.LoggingLogicImplementation;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("componentRunner")
public
class ComponentRunner {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	LoggingLogic loggingLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <ComponentManagerBuilder> componentManagerBuilderProvider;

	// properties

	@Getter @Setter
	String primaryProjectName;

	@Getter @Setter
	String primaryProjectPackageName;

	@Getter @Setter
	List <String> layerNames;

	@Getter @Setter
	List <String> configNames;

	@Getter @Setter
	String runnerName;

	@Getter @Setter
	String methodName;

	@Getter @Setter
	List <String> runnerArgs;

	// state

	Class <?> runnerClass;

	// implementation

	private
	void run (
			@NonNull List <String> arguments) {

		LoggingLogic loggingLogic =
			new LoggingLogicImplementation (
				false,
				ImmutableList.of (
					new Log4jLogTargetFactory ()));

		try (

			BootstrapComponentManager bootstrapComponentManager =
				new BootstrapComponentManager (
					loggingLogic);

			OwnedTaskLogger taskLogger =
				bootstrapComponentManager.bootstrapTaskLogger (
					this);

		) {

			bootstrapComponentManager.registerStandardClasses (
				taskLogger);

			bootstrapComponentManager.bootstrapComponent (
				taskLogger,
				this);

			processArguments (
				taskLogger,
				arguments);

			runnerClass =
				classForNameRequired (
					runnerName);

			runComponent (
				bootstrapComponentManager,
				taskLogger);

		}

	}

	private
	void processArguments (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runComponent");

		) {

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

			this

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

			;

		}

	}

	private
	void runComponent (
			@NonNull ComponentManager bootstrapComponentManager,
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"run");

		) {

			try (

				ComponentManager componentManager =
					initComponentManager (
						bootstrapComponentManager,
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

	private
	ComponentManager initComponentManager (
			@NonNull ComponentManager bootstrapComponentManager,
			@NonNull TaskLogger parentTaskLogger)
		throws Exception {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initComponentManager");

		) {

			return componentManagerBuilderProvider.get ()

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
			@NonNull String[] arguments) {

		ComponentRunner componentRunner =
			new ComponentRunner ();

		componentRunner.run (
			ImmutableList.copyOf (
				arguments));

	}

}
