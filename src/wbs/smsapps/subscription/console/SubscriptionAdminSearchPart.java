package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("subscriptionAdminSearchPart")
public
class SubscriptionAdminSearchPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	void renderHtmlBodyContent () {

		String localName =
			requestContext.stuffInt ("subscriptionAffiliateId") != null
				? "/subscriptionAffiliate.admin.search"
				: "/subscription.admin.search";

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (localName),
			">\n");

		printFormat (
			"<table class=\"details\">");

		printFormat (
			"<tr>\n",

			"<th>Number</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"number\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_number")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Active on date</th>\n",

			"<td><input",
			" id=\"active_date\"",
			" type=\"text\"",
			" name=\"active_date\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_active_date")),
			"><input",
			" type=\"button\"",
			" onclick=\"%h\"",
			stringFormat (
				"document.getElementById ('active_date').value = '%j'",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					Instant.now ())),
			" value=\"now\"",
			"></td>\n",

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Started date (from/to)</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"started_from\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_started_from")),
			">",

			"<input",
			" type=\"text\"",
			" name=\"started_to\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_started_to")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Ended date (from/to)</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"ended_from\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_ended_from")),
			">",

			"<input",
			" type=\"text\"",
			" name=\"ended_to\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"subscription_sub_search_ended_to")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search now\"",
			"></p>");

	}

}
