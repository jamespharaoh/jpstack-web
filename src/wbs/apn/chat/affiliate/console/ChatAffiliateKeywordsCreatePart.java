package wbs.apn.chat.affiliate.console;

import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethodAction;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

import javax.inject.Named;

import wbs.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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

		htmlFormOpenMethodAction (
			"post",
			requestContext.resolveLocalUrl (
				"/chatAffiliate.keywords.create"));

		htmlTableOpenDetails ();

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

		htmlTableDetailsRowWriteHtml (
			"Join type",
			() -> chatKeywordJoinTypeConsoleHelper.writeSelect (
				"joinType",
				requestContext.getForm (
					"joinType")));

		htmlTableDetailsRowWriteHtml (
			"Gender",
			() -> {

			genderConsoleHelper.writeSelect (
				"gender",
				requestContext.getForm (
					"gender"));

			formatWriter.writeLineFormat (
				"(optional)");

		});

		htmlTableDetailsRowWriteHtml (
			"Orient",
			() -> {

			orientConsoleHelper.writeSelect (
				"orient",
				requestContext.getForm (
					"orient"));

			formatWriter.writeLineFormat (
				"(optional)");

		});

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"create keyword\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

}
