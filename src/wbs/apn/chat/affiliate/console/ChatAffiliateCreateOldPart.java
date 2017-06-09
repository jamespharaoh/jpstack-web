package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;

@PrototypeComponent ("chatAffiliateCreateOldPart")
public
class ChatAffiliateCreateOldPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	@NamedDependency
	EnumConsoleHelper <?> genderConsoleHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@NamedDependency
	EnumConsoleHelper <?> orientConsoleHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	Map <String, Long> chatSchemes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			ChatRec chat =
				chatHelper.findFromContextRequired (
					transaction);

			chatSchemes =
				chat.getChatSchemes ().stream ()

				.filter (
					chatScheme ->
						privChecker.canRecursive (
							transaction,
							chatScheme,
							"affiliate_create"))

				.collect (
					Collectors.toMap (
						chatScheme ->
							objectManager.objectPathMini (
								transaction,
								chatScheme,
								chat),
						ChatSchemeRec::getId));

		}

	}

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

			if (chatSchemes.size () == 0) {

				formatWriter.writeFormat (
					"<p>There are no schemes in which you have permission to ",
					"create new affiliates.</p>");

				return;

			}

			renderForm (
				formatWriter);

		}

	}

	private
	void renderForm (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeFormat (
			"<p>Please select the scheme in which to create the affiliate, ",
			"and choose a unique name to identify it.</p>");

		// open form

		htmlFormOpenPostAction (
			formatWriter,
			requestContext.resolveLocalUrl (
				"/chatAffiliate.create.old"));

		// main elements

		renderMainElements (
			formatWriter);

		renderKeywords (
			formatWriter);

		// controls

		htmlParagraphOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"create affiliate\"",
			">");

		htmlParagraphClose (
			formatWriter);

		// close form

		htmlFormClose (
			formatWriter);

	}

	private
	void renderMainElements (
			@NonNull FormatWriter formatWriter) {

		// open table

		htmlTableOpenDetails (
			formatWriter);

		// scheme

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			"Scheme");

		htmlTableCellOpen (
			formatWriter);

		htmlSelectOpen (
			formatWriter,
			"chatScheme");

		htmlOptionWrite (
			formatWriter,
			"",
			false,
			"");

		for (
			Map.Entry <String, Long> schemeEntry
				: chatSchemes.entrySet ()
		) {

			htmlOptionWrite (
				formatWriter,
				integerToDecimalString (
					schemeEntry.getValue ()),
				stringEqualSafe (
					schemeEntry.getValue ().toString (),
					requestContext.formOrEmptyString (
						"chatScheme")),
				schemeEntry.getKey ());

		}

		htmlSelectClose (
			formatWriter);

		htmlTableCellClose (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

		// name

		htmlTableDetailsRowWriteHtml (
			formatWriter,
			"Name",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"name\"",
				" size=\"32\"",
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"name"),
				">"));

		// description

		htmlTableDetailsRowWriteHtml (
			formatWriter,
			"Description",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"description\"",
				" size=\"32\"",
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"description"),
				">"));

		// close table

		htmlTableClose (
			formatWriter);

	}

	private
	void renderKeywords (
			@NonNull FormatWriter formatWriter) {

		htmlHeadingTwoWrite (
			formatWriter,
			"Keywords");

		htmlParagraphWriteFormat (
			formatWriter,
			"You can optionally create some join keywords for this affiliate ",
			"at this point. If not, please remember to create some later.");

		htmlTableOpenList (
			formatWriter);

		htmlTableHeaderRowWrite (
			formatWriter,
			"Keyword",
			"Join type",
			"Gender",
			"Orient");

		for (
			int index = 0;
			index < 3;
			index ++
		) {

			// open row

			htmlTableRowOpen (
				formatWriter);

			// keyword

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"keyword%h\"",
				integerToDecimalString (
					index),
				" value=\"%h\"",
				requestContext.formOrEmptyString (
					"keyword" + index),
			">");

			htmlTableCellClose (
				formatWriter);

			// join type

			htmlTableCellOpen (
				formatWriter);

			chatKeywordJoinTypeConsoleHelper.writeSelect (
				formatWriter,
				"joinType" + index,
				requestContext.formOrEmptyString (
					"joinType" + index));

			htmlTableCellClose (
				formatWriter);

			// gender

			htmlTableCellOpen (
				formatWriter);

			genderConsoleHelper.writeSelect (
				formatWriter,
				"gender" + index,
				requestContext.formOrEmptyString (
					"gender" + index));

			htmlTableCellClose (
				formatWriter);

			// orient

			htmlTableCellOpen (
				formatWriter);

			orientConsoleHelper.writeSelect (
				formatWriter,
				"orient" + index,
				requestContext.formOrEmptyString (
					"orient" + index));

			htmlTableCellClose (
				formatWriter);

			// close row

			htmlTableRowClose (
				formatWriter);

		}

		htmlTableClose (
			formatWriter);

	}

}
