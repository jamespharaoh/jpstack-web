package wbs.integrations.oxygen8.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("oxygen8RouteSummaryAdditionalPart")
public
class Oxygen8RouteSummaryAdditionalPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	Oxygen8RouteOutObjectHelper oxygen8RouteOutHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	RouteRec route;
	Oxygen8RouteOutRec oxygen8RouteOut;

	// implementation

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

		oxygen8RouteOut =
			oxygen8RouteOutHelper.findRequired (
				route.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlHeadingTwoWrite (
			"Oxygen8 route information");

		htmlTableOpenDetails ();

		if (
			isNotNull (
				oxygen8RouteOut)
		) {

			htmlTableDetailsRowWrite (
				"Relay URL",
				oxygen8RouteOut.getRelayUrl ());

			htmlTableDetailsRowWrite (
				"Shortcode",
				oxygen8RouteOut.getShortcode ());

			htmlTableDetailsRowWrite (
				"Premium",
				booleanToYesNo (
					oxygen8RouteOut.getPremium ()));

			htmlTableDetailsRowWrite (
				"Username",
				oxygen8RouteOut.getUsername ());

			htmlTableDetailsRowWrite (
				"Password",
				ifThenElse (
					requestContext.canContext ("route.manage"),
					() -> oxygen8RouteOut.getPassword (),
					() -> "**********"));

			htmlTableRowClose ();

		}

		if (route.getCanReceive ()) {

			htmlTableDetailsRowWrite (
				"Inbound URL",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/oxygen8",
					"/route",
					"/%u",
					route.getId (),
					"/in"));

		}

		if (
			oxygen8RouteOut != null
			&& route.getCanSend ()
			&& route.getDeliveryReports ()
		) {

			htmlTableDetailsRowWrite (
				"Delivery reports URL",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/oxygen8",
					"/route",
					"/%u",
					route.getId (),
					"/report"));

		}

		htmlTableClose ();

	}

}
