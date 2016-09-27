package wbs.integrations.digitalselect.console;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
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
	void prepare () {

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
	void renderHtmlBodyContent () {

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
					"/%d",
					route.getId (),
					"/report"));

		}

		htmlTableClose ();

	}

}
