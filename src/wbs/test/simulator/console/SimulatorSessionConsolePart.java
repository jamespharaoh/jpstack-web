package wbs.test.simulator.console;

import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlDivClose;
import static wbs.utils.web.HtmlBlockUtils.htmlDivOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.html.SelectBuilder;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("simulatorSessionConsolePart")
public
class SimulatorSessionConsolePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	NetworkConsoleHelper networkHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SelectBuilder> selectBuilderProvider;

	// state

	Map <String, String> routeOptions =
		new LinkedHashMap<> ();

	Map <String, String> networkOptions =
		new LinkedHashMap<> ();

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

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> builder ()

			.addAll (
				super.links ())

			.add (
				HtmlLink.applicationCssStyle (
					"/style/simulator-console.css"))

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
	void renderHtmlBodyContent () {

		htmlDivOpen (
			htmlClassAttribute (
				"simulator"),
			htmlDataAttribute (
				"create-event-url",
				requestContext.resolveLocalUrl (
					"/simulatorSession.createEvent")),
			htmlDataAttribute (
				"poll-url",
				requestContext.resolveLocalUrl (
					"/simulatorSession.poll")));

		controls ();
		eventsList ();

		htmlDivClose ();

	}

	void controls () {

		htmlTableOpenDetails ();

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

		htmlTableDetailsRowWriteHtml (
			"Network",
			() -> selectBuilderProvider.get ()
				.htmlClass ("networkSelect")
				.options (networkOptions)
				.selectedValue ((String)
					requestContext.session ("simulatorNetworkId"))
				.build ());

		htmlTableDetailsRowWriteHtml (
			"Num from",
			() -> formatWriter.writeLineFormat (
				"<input",
				" class=\"numFromText\"",
				" type=\"text\"",
				" value=\"%h\">",
				emptyStringIfNull (
					(String)
					requestContext.session (
						"simulatorNumFrom"))));

		printFormat (
			"<tr>\n",
			"<th>Num to</th>\n",

			"<td>",
			"<input",
			" class=\"numToText\"",
			" type=\"text\"",
			" value=\"%h\">",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"simulatorNumTo")),
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
			emptyStringIfNull (
				(String)
				requestContext.session (
					"simulatorMessage")),
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
