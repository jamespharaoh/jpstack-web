package wbs.test.simulator.console;

import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlInputUtils.htmlSelect;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;

import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("simulatorSessionConsolePart")
public
class SimulatorSessionConsolePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkConsoleHelper networkHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// state

	Map <String, String> routeOptions =
		new LinkedHashMap<> ();

	Map <String, String> networkOptions =
		new LinkedHashMap<> ();

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			for (
				RouteRec route
					: routeHelper.findAll (
						transaction)
			) {

				routeOptions.put (
					route.getId ().toString (),
					route.getCode ());

			}

			for (
				NetworkRec network
					: networkHelper.findAll (
						transaction)
			) {

				networkOptions.put (
					network.getId ().toString (),
					network.getCode ());

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlDivOpen (
				formatWriter,
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

			controls (
				transaction,
				formatWriter);

			eventsList (
				transaction,
				formatWriter);

			htmlDivClose (
				formatWriter);

		}

	}

	private
	void controls (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"controls");

		) {

			htmlTableOpenDetails (
				formatWriter);

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
				formatWriter,
				"Network",
				() -> htmlSelect (
					formatWriter,
					"network",
					networkOptions,
					optionalOrEmptyString (
						userSessionLogic.userDataString (
							transaction,
							userConsoleLogic.userRequired (
								transaction),
							"simulator_network_id")),
					htmlClassAttribute (
						"networkSelect")));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num from",
				() -> formatWriter.writeLineFormat (
					"<input",
					" class=\"numFromText\"",
					" type=\"text\"",
					" value=\"%h\"",
					optionalOrEmptyString (
						userSessionLogic.userDataString (
							transaction,
							userConsoleLogic.userRequired (
								transaction),
							"simulator_num_from")),
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num to",
				() -> formatWriter.writeLineFormat (
					"<input",
					" class=\"numToText\"",
					" type=\"text\"",
					" value=\"%h\"",
					optionalOrEmptyString (
						userSessionLogic.userDataString (
							transaction,
							userConsoleLogic.userRequired (
								transaction),
							"simulator_num_to")),
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Message",
				() -> formatWriter.writeLineFormat (
					"<input",
					" class=\"messageText\"",
					" type=\"text\"",
					" value=\"%h\"",
					optionalOrEmptyString (
						userSessionLogic.userDataString (
							transaction,
							userConsoleLogic.userRequired (
								transaction),
							"simulator_message")),
					">"));

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<button",
				" class=\"sendButton\"",
				" type=\"submit\"",
				">send message</button>");

			htmlParagraphClose (
				formatWriter);

		}

	}

	void eventsList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"eventsList");

		) {

			htmlTableOpen (
				formatWriter,
				htmlClassAttribute (
					"list",
					"events"));

			htmlTableHeadOpen (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Date",
				"Time",
				"Type",
				"Details",
				"Actions");

			htmlTableHeadClose (
				formatWriter);

			htmlTableBodyOpen (
				formatWriter);

			htmlTableBodyClose (
				formatWriter);

			htmlTableClose (
				formatWriter);

		}

	}

}
