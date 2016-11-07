package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlEncodeNonBreakingWhitespace;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.html.HtmlLink;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.utils.time.TimeFormatter;

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
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	GazetteerLogic gazetteerLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	Transaction transaction;

	Collection<ChatUserRec> users;

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
	void prepare () {

		transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		users =
			new TreeSet<ChatUserRec> (
				ChatUserOnlineComparator.INSTANCE);

		users.addAll (
			chatUserHelper.findOnline (
				chat));

		targetContextType =
			consoleManager.contextType (
				"chatUser:combo",
				true);

		targetContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				targetContextType);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
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
				htmlClassAttribute (
					"magic-table-row"),
				htmlDataAttribute (
					"target-href",
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%u",
							chatUser.getId ()))));

			htmlTableCellWrite (
				chatUser.getCode ());

			chatConsoleLogic.writeTdForChatUserTypeShort (
				chatUser);

			chatConsoleLogic.writeTdForChatUserGenderShort (
				chatUser);

			chatConsoleLogic.writeTdForChatUserOrientShort (
				chatUser);

			htmlTableCellWriteHtml (
				ifNotNullThenElseEmDash (
					chatUser.getName (),
					() -> htmlEncodeNonBreakingWhitespace (
						chatUser.getName ())));

			if (! chatUser.getChatUserImageList ().isEmpty ()) {

				htmlTableCellOpen ();

				mediaConsoleLogic.writeMediaThumb32 (
					chatUser.getChatUserImageList ().get (0).getMedia ());

				htmlTableCellClose ();

			} else {

				htmlTableCellWrite (
					"—");

			}

			htmlTableCellWrite (
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
				placeName);

			if (
				isNotNull (
					chatUser.getLastAction ())
			) {

				htmlTableCellWriteHtml (
					htmlEncodeNonBreakingWhitespace (
						timeFormatter.prettyDuration (
							chatUser.getLastAction (),
							transaction.now ())));

			} else {

				htmlTableCellWrite (
					"—");

			}

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
