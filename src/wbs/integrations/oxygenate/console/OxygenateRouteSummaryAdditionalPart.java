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
import wbs.console.request.ConsoleRequestContext;

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

import wbs.utils.string.FormatWriter;

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
	ConsoleRequestContext requestContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Oxygen8 route information");

			htmlTableOpenDetails (
				formatWriter);

			if (
				isNotNull (
					oxygen8RouteOut)
			) {

				htmlTableDetailsRowWrite (
					formatWriter,
					"Relay URL",
					oxygen8RouteOut.getRelayUrl ());

				htmlTableDetailsRowWrite (
					formatWriter,
					"Shortcode",
					oxygen8RouteOut.getShortcode ());

				htmlTableDetailsRowWrite (
					formatWriter,
					"Premium",
					booleanToYesNo (
						oxygen8RouteOut.getPremium ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Username",
					oxygen8RouteOut.getUsername ());

				htmlTableDetailsRowWrite (
					formatWriter,
					"Password",
					ifThenElse (
						requestContext.canContext ("route.manage"),
						() -> oxygen8RouteOut.getPassword (),
						() -> "**********"));

				htmlTableRowClose (
					formatWriter);

			}

			if (route.getCanReceive ()) {

				htmlTableDetailsRowWrite (
					formatWriter,
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
					formatWriter,
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

			htmlTableClose (
				formatWriter);

		}

	}

}
