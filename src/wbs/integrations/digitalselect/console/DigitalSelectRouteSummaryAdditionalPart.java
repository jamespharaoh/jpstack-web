package wbs.integrations.digitalselect.console;

import static wbs.utils.string.StringUtils.stringFormat;

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

		printFormat (
			"<h2>Digital Select route information</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		if (digitalSelectRouteOut != null) {

			printFormat (
				"<tr>\n",
				"<th>URL</th>\n",
				"<td>%h</td>\n",
				digitalSelectRouteOut.getUrl (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Username</th>\n",
				"<td>%h</td>\n",
				digitalSelectRouteOut.getUsername (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Password</th>\n",
				"<td>%h</td>\n",
				requestContext.canContext ("route.manage")
					? digitalSelectRouteOut.getPassword ()
					: "**********",
				"</tr>\n");

		}

		if (
			digitalSelectRouteOut != null
			&& route.getCanSend ()
			&& route.getDeliveryReports ()
		) {

			printFormat (
				"<tr>\n",
				"<th>Delivery reports URL</th>\n",
				"<td>%h</td>\n",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/digitalselect",
					"/route",
					"/%d",
					route.getId (),
					"/report"));

		}

		printFormat (
			"</table>\n");

	}

}
