package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.clients.apn.chat.core.console.GenderConsoleHelper;
import wbs.clients.apn.chat.core.console.OrientConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatAffiliateKeywordsCreatePart")
public
class ChatAffiliateKeywordsCreatePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@Inject
	GenderConsoleHelper genderConsoleHelper;

	@Inject
	OrientConsoleHelper orientConsoleHelper;

	// implementation

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatAffiliate.keywords.create"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Keyword</th>\n",

			"<td>%s</td>\n",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"keyword\"",
				" value=\"%h\"",
				emptyStringIfNull (
					requestContext.getForm ("keyword")),
				">"),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Join type</th>\n",

			"<td>%s</td>\n",
			chatKeywordJoinTypeConsoleHelper.select (
				"joinType",
				requestContext.getForm ("joinType")),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Gender</th>\n",

			"<td>%s (optional)</td>\n",
			genderConsoleHelper.select (
				"gender",
				requestContext.getForm ("gender")),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Orient</th>",
			"<td>%s (optional)</td>\n",
			orientConsoleHelper.select (
				"orient",
				requestContext.getForm ("orient")),
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create keyword\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
