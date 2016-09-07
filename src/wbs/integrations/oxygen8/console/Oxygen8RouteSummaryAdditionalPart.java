package wbs.integrations.oxygen8.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.config.WbsConfig;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("oxygen8RouteSummaryAdditionalPart")
public
class Oxygen8RouteSummaryAdditionalPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	Oxygen8RouteOutObjectHelper oxygen8RouteOutHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
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

		printFormat (
			"<h2>Oxygen8 route information</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		if (oxygen8RouteOut != null) {

			printFormat (
				"<tr>\n",
				"<th>Relay URL</th>\n",
				"<td>%h</td>\n",
				oxygen8RouteOut.getRelayUrl (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Shortcode</th>\n",
				"<td>%h</td>\n",
				oxygen8RouteOut.getShortcode (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Premium</th>\n",
				"<td>%h</td>\n",
				oxygen8RouteOut.getPremium () ? "yes" : "no",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Username</th>\n",
				"<td>%h</td>\n",
				oxygen8RouteOut.getUsername (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Password</th>\n",
				"<td>%h</td>\n",
				requestContext.canContext ("route.manage")
					? oxygen8RouteOut.getPassword ()
					: "**********",
				"</tr>\n");

		}

		if (route.getCanReceive ()) {

			printFormat (
				"<tr>\n",
				"<th>Inbound URL</th>\n",
				"<td>%h</td>\n",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/oxygen8",
					"/route",
					"/%u",
					route.getId (),
					"/in"),
				"</tr>\n");

		}

		if (
			oxygen8RouteOut != null
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
					"/oxygen8",
					"/route",
					"/%u",
					route.getId (),
					"/report"));

		}

		printFormat (
			"</table>\n");

	}

}
