package wbs.platform.script.system.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.PathHandler;
import wbs.framework.web.WebFile;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.script.system.model.SystemScriptRec;

@Log4j
@SingletonComponent ("systemScriptRunPathHandler")
public
class SystemScriptRunPathHandler
	implements PathHandler {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	SystemScriptConsoleHelper systemScriptHelper;

	@Inject
	Provider<ConsoleFile> consoleFile;

	final static
	Pattern runPattern =
		Pattern.compile ("/([a-zA-Z0-9_]+)");

	@Override
	public
	WebFile processPath (
			String path) {

		log.debug ("Got path [" + path + "]");

		Matcher matcher =
			runPattern.matcher (path);

		if (! matcher.matches ())
			return null;

		String code = matcher.group (1);

		log.debug ("Found code [" + code + "]");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		SystemScriptRec systemScript =
			systemScriptHelper.findByCode (
				GlobalId.root,
				code);

		if (systemScript == null)
			return null;

		requestContext.request (
			"systemScriptId",
			systemScript.getId ());

		log.debug (
			stringFormat (
				"Got system script %s",
				systemScript.getId ()));

		return consoleFile.get ()

			.getHandlerName (
				"systemScriptStandaloneRequestHandler")

			.postHandlerName (
				"systemScriptStandaloneRequestHandler");

	}

}
