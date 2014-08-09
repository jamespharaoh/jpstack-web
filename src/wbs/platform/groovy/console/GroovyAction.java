package wbs.platform.groovy.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("groovyAction")
public
class GroovyAction
	extends ConsoleAction {

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleRequestContext requestContext;

	@Override
	public
	Responder backupResponder () {
		return responder ("groovyResponder");
	}

	@Override
	protected
	Responder goReal () {

		// create a groovy binding containing all the beans

		Binding binding =
			new Binding ();

		Map<String,Object> map =
			applicationContext
				.getAllSingletonBeans ();

		for (Map.Entry<String,Object> entry
				: map.entrySet ()) {

			binding.setProperty (
				entry.getKey (),
				entry.getValue ());

		}

		// create an "out" variable

		StringWriter out =
			new StringWriter ();

		binding.setProperty (
			"page",
			new PrintWriter (out));

		// create the groovy interpreter

		GroovyShell shell =
			new GroovyShell (binding);

		// and run the script

		Object ret =
			shell.evaluate (
				requestContext.parameter ("groovy"),
				"webscript.groovy");

		// save the output in the request

		requestContext.request (
			"result",
			ifNull (ret, "").toString ());

		requestContext.request (
			"output",
			out.toString ());

		return null;

	}

}
