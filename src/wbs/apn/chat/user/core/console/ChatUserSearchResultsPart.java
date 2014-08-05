package wbs.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.sum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.console.ChatUserCreditModeConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.framework.utils.etc.StringFormatter;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.html.HtmlLink;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.PageBuilder;
import wbs.platform.console.misc.Percentager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("chatUserSearchResultsPart")
public
class ChatUserSearchResultsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatUserCreditModeConsoleHelper chatUserCreditModeConsoleHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	TimeFormatter timeFormatter;

	// value dependencies

	@Inject
	@Named ("chatUserSearchItemsPerSubPage")
	Integer itemsPerSubPage;

	@Inject
	@Named ("chatUserSearchSubPagesPerPage")
	Integer subPagesPerPage;

	// state

	ConsoleContext targetContext;

	List<ChatUserRec> chatUsers;

	PageBuilder pageBuilders [] =
		new PageBuilder [] {
			new PageBuilder (),
			new PageBuilder ()
		};

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery-1.7.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/page-builder.js"))

			.build ();

	}

	@Override
	public
	Set<HtmlLink> links () {

		return ImmutableSet.<HtmlLink>of (
			HtmlLink.cssStyle (
				requestContext.resolveApplicationUrl (
					"/js/chat.js")));

	}

	// implementation

	@Override
	public
	void prepare () {

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				"chatUser+",
				true);

		targetContext =
			consoleManager.relatedContext (
				requestContext.consoleContext (),
				targetContextType);

		List<?> chatUserSearchResult =
			(List<?>)
			requestContext.request ("chatUserSearchResult");

		chatUsers =
			new ArrayList<ChatUserRec> ();

		for (Object chatUserIdObject
				: chatUserSearchResult) {

			chatUsers.add (
				chatUserHelper.find (
					(Integer) chatUserIdObject));

		}

		prepare0 ();

		prepare1 ();

	}

	private
	void prepare0 () {

		PageBuilder pageBuilder =
			pageBuilders [0];

		StringFormatter.printWriterFormat (
			pageBuilder.headWriter (),

			"<table class=\"list\">\n",

			"<tr>\n",
			"<th>User</th>\n",
			"<th>T</th>\n",
			"<th>G</th>\n",
			"<th>O</th>\n",
			"<th>Name</th>\n",
			"<th>Pic</th>\n",
			"<th>Info</th>\n",
			"</tr>\n");

		int index = 0;

		for (ChatUserRec chatUser
				: chatUsers) {

			StringFormatter.printWriterFormat (
				pageBuilder.writer (),

				"%s\n",
				Html.magicTr (
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%u",
							chatUser.getId ())),
					false),

				"<td>%h</td>\n",
				chatUser.getCode (),

				"%s\n",
				chatConsoleLogic.tdForChatUserTypeShort (
					chatUser),

				"%s\n",
				chatConsoleLogic.tdForChatUserGenderShort (
					chatUser),

				"%s\n",
				chatConsoleLogic.tdForChatUserOrientShort (
					chatUser),

				"<td>%h</td>\n",
				ifNull (chatUser.getName (), "-"));

			if (! chatUser.getChatUserImageList ().isEmpty ()) {

				StringFormatter.printWriterFormat (
					pageBuilder.writer (),

					"<td>%s</td>\n",
					mediaConsoleLogic.mediaThumb32 (
						chatUser.getChatUserImageList ().get (0).getMedia ()));

			} else {

				StringFormatter.printWriterFormat (
					pageBuilder.writer (),

					"<td>-</td>\n");

			}

			StringFormatter.printWriterFormat (
				pageBuilder.writer (),

				"<td>%h</td>\n",
				ifNull (
					chatUser.getInfoText (),
					"-"),

				"</tr>\n");

			if (index ++ == itemsPerSubPage) {

				pageBuilder.endPage ();

				index = 0;

			}

		}

		pageBuilder.endPage ();

		StringFormatter.printWriterFormat (
			pageBuilder.footWriter (),

			"</table>\n");

	}

	private void prepare1 () {

		PageBuilder pageBuilder =
			pageBuilders [1];

		StringFormatter.printWriterFormat (
			pageBuilder.headWriter (),

			"<table class=\"list\">\n",

			"<tr>\n",
			"<th>User</th>\n",
			"<th>T</th>\n",
			"<th>G</th>\n",
			"<th>O</th>\n",
			"<th>Spent</th>\n",
			"<th>Credit</th>\n",
			"<th>Mode</th>\n",
			"<th>Pend</th>\n",
			"<th>Succ</th>\n",
			"<th>Fail</th>\n",
			"<th>Scheme</th>\n",
			"<th>Last action</th>\n",
			"<th>U</th>\n",
			"<th>M</th>\n",
			"<th>T</th>\n",
			"<th>I</th>\n",
			"</tr>");

		int index = 0;

		for (ChatUserRec chatUser
				: chatUsers) {

			Percentager percentager =
				new Percentager ();

			percentager.add (
				chatUser.getUserMessageCharge ());

			percentager.add (
				chatUser.getMonitorMessageCharge ());

			percentager.add (
				chatUser.getTextProfileCharge ());

			percentager.add (
				chatUser.getImageProfileCharge ());

			Iterator<Integer> percentagerIterator =
				percentager.work ().iterator ();

			StringFormatter.printWriterFormat (
				pageBuilder.writer (),

				"%s\n",
				Html.magicTr (
					requestContext.resolveContextUrl (
						stringFormat (
							"/chatUser",
							"/%u",
							chatUser.getId (),
							"/chatUser.summary")),
					false),

				"<td>%h</td>\n",
				chatUser.getCode (),

				"%s\n",
				chatConsoleLogic.tdsForChatUserTypeGenderOrientShort (
					chatUser),

				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getValueSinceEver ()),

				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCredit ()),

				"%s\n",
				chatUserCreditModeConsoleHelper.toTd (
					chatUser.getCreditMode ()),

				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					sum (
						+ chatUser.getCreditPending (),
						+ chatUser.getCreditPendingStrict ())),

				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditSuccess ()),

				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditFailed ()),

				// pw.println (ChatConsoleStuff.tdForCredit
				// (cu.getCreditFailed () + cu.getCreditRevoked ()));

				"<td>%h</td>\n",
				chatUser.getChatScheme () != null
					? chatUser.getChatScheme ().getCode ()
					: "-",

				"<td>%h</td>\n",
				chatUser.getLastAction () != null
					? timeFormatter.instantToDateStringShort (
						chatUserLogic.timezone (
							chatUser),
						dateToInstant (
							chatUser.getLastAction ()))
					: "-",

				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next (),

				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next (),

				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next (),

				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next (),

				"</tr>\n");

			if (index ++ == itemsPerSubPage) {

				pageBuilder.endPage ();

				index = 0;

			}

		}

		pageBuilder.endPage ();

		StringFormatter.printWriterFormat (
			pageBuilder.footWriter (),

			"</table>");

	}

	private
	void goPages () {

		if (pageBuilders [0].pages () <= 1)
			return;

		printFormat (
			"<p",
			" class=\"links\"",
			">Select page\n");

		for (
			int page = 0;
			page < pageBuilders [0].pages ();
			page ++
		) {

			printFormat (
				"<a",
				" class=\"little-page-link-%s\"",
				page,
				" href=\"#\"",
				" onclick=\"pageBuilder.showLittlePage (%s);\"",
				page,
				">%s</a>\n",
				page + 1);

		}

		printFormat (
			"</p>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<p",
			" class=\"links\"",
			">Select mode\n",

			"<a",
			" class=\"big-page-link-0\"",
			" href=\"#\"",
			" onclick=\"pageBuilder.showBigPage (0)\"",
			">Normal</a>\n",

			"<a",
			" class=\"big-page-link-1\"",
			" href=\"#\"",
			" onclick=\"pageBuilder.showBigPage (1)\"",
			">Credit</a></p>\n");

		goPages ();

		printFormat (
			"<div id=\"pageHolder\">Please wait...</div>\n");

		goPages ();

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"var pages = [ [\n");
		pageBuilders [0].goPages (out);
		printFormat (
			"], [\n");
		pageBuilders [1].goPages (out);
		printFormat (
			"] ];\n");

		printFormat (
			"var pageBuilder =\n",
			"\tnew PageBuilder (pages);\n");

		printFormat (
			"$(function () {\n",
			"pageBuilder.init ();\n",
			"});\n");

		printFormat (
			"</script>\n");

	}

}
