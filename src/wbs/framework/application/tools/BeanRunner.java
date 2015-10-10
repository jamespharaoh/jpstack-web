package wbs.framework.application.tools;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.uncapitalise;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.BeanDefinition;

/**
 * Create an application context, instantiate a bean in it and invoke a
 * specific method with a list of string arguments. Also contains a main method
 * in order to be useful from the command line or in an ant build script.
 */
@Accessors (fluent = true)
public
class BeanRunner {

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
	List<String> runnerArgs;

	Class<?> runnerClass;

	ApplicationContext applicationContext;

	public
	void run ()
		throws Exception {

		runnerClass =
			Class.forName (runnerName);

		initApplicationContext ();

		invokeTarget ();

		applicationContext.close ();

	}

	void initApplicationContext ()
		throws Exception {

		applicationContext =
			new ApplicationContextBuilder ()

			.primaryProjectName (
				primaryProjectName)

			.primaryProjectPackageName (
				primaryProjectPackageName)

			.layerNames (
				layerNames)

			.configNames (
				configNames)

			.registerBeanDefinition (
				new BeanDefinition ()

				.beanClass (
					runnerClass)

				.name (
					uncapitalise (
						runnerClass.getSimpleName ()))

				.scope (
					"singleton"))

			.build ();

	}

	public
	void invokeTarget ()
		throws Exception {

		// find runnable and run it

		Object runner =
			applicationContext.getBean (
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

	public
	static void main (
			String[] argumentsArray)
		throws Exception {

		List<String> arguments =
			Arrays.asList (argumentsArray);

		if (arguments.size () < 5) {

			throw new RuntimeException (
				stringFormat (
					"Expects five or more parameters: %s",
					joinWithSeparator (
						", ",
						"primary project name",
						"primary project package name",
						"layer names (comma separated)",
						"config names (comma separated)",
						"runner class name",
						"runner method name",
						"runner arguments...")));

		}

		new BeanRunner ()

			.primaryProjectName (
				arguments.get (0))

			.primaryProjectPackageName (
				arguments.get (1))

			.layerNames (
				Arrays.asList (arguments.get (2).split (",")))

			.configNames (
				Arrays.asList (arguments.get (3).split (",")))

			.runnerName (
				arguments.get (4))

			.methodName (
				arguments.get (5))

			.runnerArgs (
				arguments.subList (6, arguments.size ()))

			.run ();

	}

}
