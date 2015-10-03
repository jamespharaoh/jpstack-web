package wbs.test.simulator.console;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleApplicationScriptRef;
import wbs.platform.console.html.JqueryScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.html.SelectBuilder;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("simulatorSessionConsolePart")
public
class SimulatorSessionConsolePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	NetworkConsoleHelper networkHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	Provider<SelectBuilder> selectBuilder;

	// state

	Map<String,String> routeOptions =
		new LinkedHashMap<String,String> ();

	Map<String,String> networkOptions =
		new LinkedHashMap<String,String> ();

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/simulator.js"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		for (
			RouteRec route
				: routeHelper.findAll ()
		) {

			routeOptions.put (
				route.getId ().toString (),
				route.getCode ());

		}

		for (
			NetworkRec network
				: networkHelper.findAll ()
		) {

			networkOptions.put (
				network.getId ().toString (),
				network.getCode ());

		}

	}

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style>\n",
			"table.events tr.messageIn td { background: #ccccff }\n",
			"table.events tr.messageOut td { background: #ffffcc }\n",
			"table.events tr.messageBill td { background: #ffcccc }\n",
			"</style>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<div",
			" class=\"simulator\"",
			" data-create-event-url=\"%h\"",
			requestContext.resolveLocalUrl (
				"/simulatorSession.createEvent"),
			" data-poll-url=\"%h\"",
			requestContext.resolveLocalUrl (
				"/simulatorSession.poll"),
			">\n");

		controls ();
		eventsList ();

		printFormat (
			"</div>\n");

	}

	void controls () {

		printFormat (
			"<table class=\"details\">\n");

		/*
		pf ("<tr>\n",

			"<th>Route</th>\n",

			"<td>%s</td>\n",
			selectBuilder.get ()
				.htmlClass ("routeSelect")
				.options (routeOptions)
				.selectedValue ((String)
					requestContext.getSession ("simulatorRouteId"))
				.build (),

			"</tr>\n");
		*/

		printFormat (
			"<tr>\n",
			"<th>Network</th>\n",

			"<td>%s</td>\n",
			selectBuilder.get ()
				.htmlClass ("networkSelect")
				.options (networkOptions)
				.selectedValue ((String)
					requestContext.session ("simulatorNetworkId"))
				.build (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Num from</th>\n",

			"<td>",
			"<input",
			" class=\"numFromText\"",
			" type=\"text\"",
			" value=\"%h\">",
			requestContext.session ("simulatorNumFrom"),
			"</td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Num to</th>\n",

			"<td>",
			"<input",
			" class=\"numToText\"",
			" type=\"text\"",
			" value=\"%h\">",
			requestContext.session ("simulatorNumTo"),
			"</td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td>",
			"<input",
			" class=\"messageText\"",
			" type=\"text\"",
			" value=\"%h\">",
			requestContext.session ("simulatorMessage"),
			"</td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><button",
			" class=\"sendButton\"",
			" type=\"submit\"",
			">send message</button></p>\n");

	}

	void eventsList () {

		printFormat (
			"<table class=\"list events\">\n");

		printFormat (
			"<thead>\n");

		printFormat (
			"<tr>\n",
			"<th>Date</th>\n",
			"<th>Time</th>\n",
			"<th>Type</th>\n",
			"<th>Details</th>\n",
			"<th>Actions</th>\n",
			"</tr>\n");

		printFormat (
			"</thead>\n");

		printFormat (
			"<tbody>\n");

		printFormat (
			"</table>\n");

	}

}
