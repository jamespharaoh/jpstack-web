package wbs.apn.chat.affiliate.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatAffiliate.keywords.create"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Keyword",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"keyword\"",
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"keyword")));

		htmlTableDetailsRowWriteHtml (
			"Join type",
			() -> chatKeywordJoinTypeConsoleHelper.writeSelect (
				"joinType",
				requestContext.formOrEmptyString(
					"joinType")));

		htmlTableDetailsRowWriteHtml (
			"Gender",
			() -> {

			genderConsoleHelper.writeSelect (
				"gender",
				requestContext.formOrEmptyString (
					"gender"));

			formatWriter.writeLineFormat (
				"(optional)");

		});

		htmlTableDetailsRowWriteHtml (
			"Orient",
			() -> {

			orientConsoleHelper.writeSelect (
				"orient",
				requestContext.formOrEmptyString (
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
