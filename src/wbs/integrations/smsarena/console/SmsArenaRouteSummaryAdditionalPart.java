package wbs.integrations.smsarena.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.integrations.smsarena.model.SmsArenaRouteOutObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteOutRec;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("smsArenaRouteSummaryAdditionalPart")
public class SmsArenaRouteSummaryAdditionalPart 
	extends AbstractPagePart {

	// dependencies

	@Inject
	SmsArenaRouteOutObjectHelper smsArenaRouteOutHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	WbsConfig wbsConfig;

	// state

	Integer routeId;
	RouteRec route;
	SmsArenaRouteOutRec smsArenaRouteOut;

	// implementation

	@Override
	public
	void prepare () {

		routeId =
			requestContext.stuffInt ("routeId");

		route =
			routeHelper.find (routeId);

		smsArenaRouteOut =
			smsArenaRouteOutHelper.find (
				routeId);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h2>Oxygen8 route information</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		if (smsArenaRouteOut != null) {

			printFormat (
				"<tr>\n",
				"<th>Relay URL</th>\n",
				"<td>%h</td>\n",
				smsArenaRouteOut.getRelayUrl (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Auth Key</th>\n",
				"<td>%h</td>\n",
				smsArenaRouteOut.getAuthKey (),
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
					"/sms-arena",
					"/route",
					"/%u",
					routeId,
					"/in"),
				"</tr>\n");

		}

		if (
			smsArenaRouteOut != null
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
					"/sms-arena",
					"/route",
					"/%u",
					routeId,
					"/report"));

		}

		printFormat (
			"</table>\n");

	}

}
