package wbs.platform.script.system.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.script.system.model.SystemScriptObjectHelper;
import wbs.platform.script.system.model.SystemScriptRec;

@SingletonComponent ("systemScriptLogic")
public
class SystemScriptLogicImpl
	implements SystemScriptLogic {

	@Inject
	SystemScriptObjectHelper systemScriptHelper;

	// TODO rename and extract interface and move

	Pattern includePattern =
		Pattern.compile ("#\\{include ([a-z_]+)\\}");

	@Override
	public
	String expand (
			String script) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher includeMatcher =
			includePattern.matcher (script);

		int pos = 0;

		while (includeMatcher.find (pos)) {

			stringBuilder.append (
				script.substring (
					pos,
					includeMatcher.start ()));

			String code =
				includeMatcher.group (1);

			SystemScriptRec systemScript =
				systemScriptHelper.findByCode (
					GlobalId.root,
					code);

			if (systemScript == null)
				throw new RuntimeException (
					"No such script: " + code);

			String includedSource =
				expand (systemScript.getText ());

			stringBuilder.append (includedSource);

			pos =
				includeMatcher.end ();

		}

		stringBuilder.append (
			script.substring (pos));

		return stringBuilder.toString ();

	}

}
