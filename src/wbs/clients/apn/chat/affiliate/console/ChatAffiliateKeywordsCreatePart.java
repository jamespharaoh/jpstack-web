package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Named;

import wbs.clients.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;

@PrototypeComponent ("chatAffiliateKeywordsCreatePart")
public
class ChatAffiliateKeywordsCreatePart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> genderConsoleHelper;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> orientConsoleHelper;

	// implementation

	@Override
	public
	void renderHtmlBodyContent () {

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
					requestContext.getForm (
						"keyword")),
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
