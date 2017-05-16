package wbs.integrations.oxygenate.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.integrations.oxygenate.model.OxygenateRouteOutObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteOutRec;

import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("oxygenateRouteSummaryAdditionalPart")
public
class OxygenateRouteSummaryAdditionalPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OxygenateRouteOutObjectHelper oxygen8RouteOutHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	RouteRec route;
	OxygenateRouteOutRec oxygen8RouteOut;

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

			route =
				routeHelper.findFromContextRequired (
					transaction);

			oxygen8RouteOut =
				oxygen8RouteOutHelper.findRequired (
					transaction,
					route.getId ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
						integerToDecimalString (
							route.getId ()),
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
						integerToDecimalString (
							route.getId ()),
						"/report"));

			}

			htmlTableClose ();

		}

	}

}
