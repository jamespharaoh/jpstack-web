package wbs.apn.chat.affiliate.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;

@PrototypeComponent ("chatAffiliateKeywordsCreatePart")
public
class ChatAffiliateKeywordsCreatePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@SingletonDependency
	@NamedDependency
	EnumConsoleHelper <?> genderConsoleHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency
	EnumConsoleHelper <?> orientConsoleHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatAffiliate.keywords.create"));

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Keyword",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"keyword\"",
					" value=\"%h\"",
					requestContext.formOrEmptyString (
						"keyword")));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Join type",
				() -> chatKeywordJoinTypeConsoleHelper.writeSelect (
					formatWriter,
					"joinType",
					requestContext.formOrEmptyString(
						"joinType")));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Gender",
				() -> {

				genderConsoleHelper.writeSelect (
					formatWriter,
					"gender",
					requestContext.formOrEmptyString (
						"gender"));

				formatWriter.writeLineFormat (
					"(optional)");

			});

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Orient",
				() -> {

				orientConsoleHelper.writeSelect (
					formatWriter,
					"orient",
					requestContext.formOrEmptyString (
						"orient"));

				formatWriter.writeLineFormat (
					"(optional)");

			});

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"create keyword\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

		}

	}

}
