package wbs.test.simulator.console;

import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.html.SelectBuilder;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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

		htmlTableDetailsRowWriteHtml (
			"Num to",
			() -> formatWriter.writeLineFormat (
				"<input",
				" class=\"numToText\"",
				" type=\"text\"",
				" value=\"%h\"",
				emptyStringIfNull (
					(String)
					requestContext.session (
						"simulatorNumTo")),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Message",
			() -> formatWriter.writeLineFormat (
				"<input",
				" class=\"messageText\"",
				" type=\"text\"",
				" value=\"%h\"",
				emptyStringIfNull (
					(String)
					requestContext.session (
						"simulatorMessage")),
				">"));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<button",
			" class=\"sendButton\"",
			" type=\"submit\"",
			">send message</button>");

		htmlParagraphClose ();

	}

	void eventsList () {

		htmlTableOpen (
			htmlClassAttribute (
				"list",
				"events"));

		htmlTableHeadOpen ();

		htmlTableHeaderRowHtml (
			"Date",
			"Time",
			"Type",
			"Details",
			"Actions");

		htmlTableHeadClose ();

		htmlTableBodyOpen ();

		htmlTableBodyClose ();

		htmlTableClose ();

	}

}
