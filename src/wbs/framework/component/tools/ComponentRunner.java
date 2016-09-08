package wbs.framework.component.tools;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringSplitComma;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.LoggedErrorsException;

@Accessors (fluent = true)
@Log4j
public
class ComponentRunner {

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

	ComponentManager componentManager;

	public
	void run ()
		throws Exception {

		runnerClass =
			Class.forName (
				runnerName);

		initComponentManager ();

		invokeTarget ();

		componentManager.close ();

	}

	void initComponentManager ()
		throws Exception {

		componentManager =
			new ComponentManagerBuilder ()

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

			.outputPath (
				"work/runner/components")

			.build ();

	}

	public
	void invokeTarget ()
		throws Exception {

		// find runnable and run it

		Object runner =
			componentManager.getComponentRequired (
				uncapitalise (
					runnerClass.getSimpleName ()),
				runnerClass);

		Method runMethod =
			runnerClass.getMethod (
				methodName,
				List.class);

		runMethod.invoke (
			runner,
			(Object) runnerArgs);

	}

	public static
	void main (
			@NonNull String[] argumentsArray) {

		List <String> arguments =
			Arrays.asList (
				argumentsArray);

		if (arguments.size () < 5) {

			log.error (
				stringFormat (
					"Expects five or more parameters: %s",
					joinWithCommaAndSpace (
						"primary project name",
						"primary project package name",
						"layer names (comma separated)",
						"config names (comma separated)",
						"runner class name",
						"runner method name",
						"runner arguments...")));

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

				.run ();

		} catch (LoggedErrorsException loggedErrorsException) {

			doNothing ();

			System.exit (1);

		} catch (Exception exception) {

			log.error (
				"Failed to run component",
				exception);

			System.exit (1);

		}

	}

}
