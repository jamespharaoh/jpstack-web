package wbs.platform.pyp.console;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.platform.console.request.ConsoleRequestContext;

@SingletonComponent ("pyp")
public
class Pyp {

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleRequestContext requestContext;

	public
	String translate (
			String script) {

		// do the translation

		StringBuilder stringBuilder =
			new StringBuilder ();

		int position = 0;

		Matcher matcher =
			pattern.matcher (script);

		while (matcher.find ()) {

			stringBuilder.append (
				script.substring (
					position,
					matcher.start ()));

			String match =
				matcher.group ();

			if (match.equals ("\"<="))
				stringBuilder.append ("\\\"\"\"\" + str (");

			else if (match.equals ("<="))
				stringBuilder.append ("\"\"\" + str (");

			else if (match.equals ("\"<q="))
				stringBuilder.append ("\\\"\"\"\" + htmlEncode (");

			else if (match.equals ("<q="))
				stringBuilder.append ("\"\"\" + htmlEncode (");

			else if (match.equals ("=>"))
				stringBuilder.append (") + \"\"\"");

			position =
				matcher.end ();

		}

		stringBuilder.append (
			script.substring (position));

		return stringBuilder.toString ();

	}

	public
	void execute (
			String script) {

		// translate the script

		String translatedScript =
			translate (script);

		// make sure jython is all set up right

		init ();

		// power up the interpreter

		PythonInterpreter interpreter =
			new PythonInterpreter ();

		// setup global variables

		Map<String,Object> map =
			applicationContext.getAllSingletonBeans ();

		for (Map.Entry<String,Object> entry
				: map.entrySet ()) {

			interpreter.set (
				entry.getKey (),
				entry.getValue ());

		}

		// now run it

		interpreter.exec (translatedScript);

	}

	private
	boolean inited = false;

	public synchronized
	void init () {

		if (inited)
			return;

		Properties props =
			new Properties ();

		props.setProperty (
			"python.modules.builtin",
			"re");

		PySystemState.initialize (
			System.getProperties (),
			props,
			new String [0]);

		String rootPath;

		rootPath =
			requestContext.realPath ("/");

		if (! rootPath.endsWith (File.separator))
			rootPath += File.separator;

		PySystemState.add_classdir (
			rootPath + "WEB-INF" + File.separator + "classes");

		PySystemState.add_extdir (
			rootPath + "WEB-INF" + File.separator + "lib",
			true);

	}

	private final
	static Pattern pattern =
		Pattern.compile ("\"?<q?=|=>");

}
