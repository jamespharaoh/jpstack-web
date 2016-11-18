package wbs.integrations.digitalselect.console;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.digitalselect.model.DigitalSelectRouteOutRec;

import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("digitalSelectRouteSummaryAdditionalPart")
public
class DigitalSelectRouteSummaryAdditionalPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	DigitalSelectRouteOutConsoleHelper digitalSelectRouteOutHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	RouteRec route;
	DigitalSelectRouteOutRec digitalSelectRouteOut;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

		digitalSelectRouteOut =
			digitalSelectRouteOutHelper.findRequired (
				route.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingTwoWrite (
			"Digital Select route information");

		htmlTableOpenDetails ();

		if (digitalSelectRouteOut != null) {

			htmlTableDetailsRowWrite (
				"URL",
				digitalSelectRouteOut.getUrl ());

			htmlTableDetailsRowWrite (
				"Username",
				digitalSelectRouteOut.getUsername ());

			htmlTableDetailsRowWrite (
				"Password",
				ifThenElse (
					requestContext.canContext ("route.manage"),
					() -> digitalSelectRouteOut.getPassword (),
					() -> "**********"));

		}

		if (
			digitalSelectRouteOut != null
			&& route.getCanSend ()
			&& route.getDeliveryReports ()
		) {

			htmlTableDetailsRowWrite (
				"Delivery reports URL",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/digitalselect",
					"/route",
					"/%s",
					integerToDecimalString (
						route.getId ()),
					"/report"));

		}

		htmlTableClose ();

	}

}
