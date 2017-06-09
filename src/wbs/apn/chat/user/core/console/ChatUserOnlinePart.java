package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlEncodeNonBreakingWhitespace;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.html.HtmlLink;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.sms.gazetteer.logic.GazetteerLogic;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserOnlinePart")
public
class ChatUserOnlinePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	GazetteerLogic gazetteerLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	BorrowedTransaction transaction;

	Collection <ChatUserRec> users;

	ConsoleContextType targetContextType;
	ConsoleContext targetContext;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> builder ()

			.addAll (
				super.links ())

			.add (
				HtmlLink.applicationCssStyle (
					"/style/chat-user-online.css"))

			.build ();

	}

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

			users =
				new TreeSet<> (
					ChatUserOnlineComparator.INSTANCE);

			users.addAll (
				chatUserHelper.findOnline (
					transaction,
					chat));

			targetContextType =
				consoleManager.contextType (
					"chatUser:combo",
					true);

			targetContext =
				consoleManager.relatedContextRequired (
					transaction,
					requestContext.consoleContextRequired (),
					targetContextType);

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

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"User",
				"T",
				"G",
				"O",
				"Name",
				"Pic",
				"Info",
				"Loc",
				"Idle");

			for (
				ChatUserRec chatUser
					: users
			) {

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						"magic-table-row"),
					htmlDataAttribute (
						"target-href",
						requestContext.resolveContextUrlFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%u",
							integerToDecimalString (
								chatUser.getId ()))));

				htmlTableCellWrite (
					formatWriter,
					chatUser.getCode ());

				chatConsoleLogic.writeTdForChatUserTypeShort (
					formatWriter,
					chatUser);

				chatConsoleLogic.writeTdForChatUserGenderShort (
					formatWriter,
					chatUser);

				chatConsoleLogic.writeTdForChatUserOrientShort (
					formatWriter,
					chatUser);

				htmlTableCellWriteHtml (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatUser.getName (),
						() -> htmlEncodeNonBreakingWhitespace (
							chatUser.getName ())));

				if (! chatUser.getChatUserImageList ().isEmpty ()) {

					htmlTableCellOpen (
						formatWriter);

					mediaConsoleLogic.writeMediaThumb32 (
						transaction,
						formatWriter,
						chatUser.getChatUserImageList ().get (0).getMedia ());

					htmlTableCellClose (
						formatWriter);

				} else {

					htmlTableCellWrite (
						formatWriter,
						"—");

				}

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatUser.getInfoText (),
						() -> spacify (
							chatUser.getInfoText ().getText ())));

				String placeName =
					"—";

				if (chatUser.getLocationLongLat () != null) {

					GazetteerEntryRec gazetteerEntry =
						gazetteerLogic.findNearestCanonicalEntry (
							chatUser.getChat ().getGazetteer (),
							chatUser.getLocationLongLat ());

					placeName =
						gazetteerEntry.getName ();

				}

				htmlTableCellWrite (
					formatWriter,
					placeName);

				if (
					isNotNull (
						chatUser.getLastAction ())
				) {

					htmlTableCellWriteHtml (
						formatWriter,
						htmlEncodeNonBreakingWhitespace (
							timeFormatter.prettyDuration (
								chatUser.getLastAction (),
								transaction.now ())));

				} else {

					htmlTableCellWrite (
						formatWriter,
						"—");

				}

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
