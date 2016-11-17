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

import javax.inject.Named;

import lombok.NonNull;

import wbs.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatAffiliateCreateOldPart")
public
class ChatAffiliateCreateOldPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> genderConsoleHelper;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> orientConsoleHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	Map <String, Long> chatSchemes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		chatSchemes =
			chat.getChatSchemes ().stream ()

			.filter (
				chatScheme ->
					privChecker.canRecursive (
						chatScheme,
						"affiliate_create"))

			.collect (
				Collectors.toMap (
					chatScheme ->
						objectManager.objectPathMini (
							chatScheme,
							chat),
					ChatSchemeRec::getId));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		if (chatSchemes.size () == 0) {

			formatWriter.writeFormat (
				"<p>There are no schemes in which you have permission to ",
				"create new affiliates.</p>");

			return;

		}

		renderForm ();

	}

	private
	void renderForm () {

		formatWriter.writeFormat (
			"<p>Please select the scheme in which to create the affiliate, ",
			"and choose a unique name to identify it.</p>");

		// open form

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatAffiliate.create.old"));

		// main elements

		renderMainElements ();

		renderKeywords ();

		// controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"create affiliate\"",
			">");

		htmlParagraphClose ();

		// close form

		htmlFormClose ();

	}

	private
	void renderMainElements () {

		// open table

		htmlTableOpenDetails ();

		// scheme

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Scheme");

		htmlTableCellOpen ();

		htmlSelectOpen (
			"chatScheme");

		htmlOptionWrite (
			"",
			false,
			"");

		for (
			Map.Entry <String, Long> schemeEntry
				: chatSchemes.entrySet ()
		) {

			htmlOptionWrite (
				integerToDecimalString (
					schemeEntry.getValue ()),
				stringEqualSafe (
					schemeEntry.getValue ().toString (),
					requestContext.formOrEmptyString (
						"chatScheme")),
				schemeEntry.getKey ());

		}

		htmlSelectClose ();

		htmlTableCellClose ();

		htmlTableRowClose ();

		// name

		htmlTableDetailsRowWriteHtml (
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

		htmlTableClose ();

	}

	private
	void renderKeywords () {

		htmlHeadingTwoWrite (
			"Keywords");

		htmlParagraphWriteFormat (
			"You can optionally create some join keywords for this affiliate ",
			"at this point. If not, please remember to create some later.");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
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

			htmlTableRowOpen ();

			// keyword

			htmlTableCellOpen ();

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

			htmlTableCellClose ();

			// join type

			htmlTableCellOpen ();

			chatKeywordJoinTypeConsoleHelper.writeSelect (
				"joinType" + index,
				requestContext.formOrEmptyString (
					"joinType" + index));

			htmlTableCellClose ();

			// gender

			htmlTableCellOpen ();

			genderConsoleHelper.writeSelect (
				"gender" + index,
				requestContext.formOrEmptyString (
					"gender" + index));

			htmlTableCellClose ();

			// orient

			htmlTableCellOpen ();

			orientConsoleHelper.writeSelect (
				"orient" + index,
				requestContext.formOrEmptyString (
					"orient" + index));

			htmlTableCellClose ();

			// close row

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
