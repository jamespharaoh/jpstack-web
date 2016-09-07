package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.StringUtils.spacify;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

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
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

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
	void renderHtmlHeadContent () {

		printFormat (

			"<style type=\"text/css\">\n",

			"table.list td.chat-user-type-user {\n",
			"  text-align: center;\n",
			"  background: #cccccc;\n",
			"  color: black;\n",
			"}\n",

			"table.list td.chat-user-type-monitor {\n",
			"  text-align: center;\n",
			"  background: #999999;\n",
			"  color: white;\n",
			"}\n",

			"table.list td.gender-male {\n",
			"  text-align: center;\n",
			"  background: #ccccff;\n",
			"  color: black;\n",
			"}\n",

			"table.list td.gender-female {\n",
			"  text-align: center;\n",
			"  background: #ffcccc;\n",
			"  color: black;\n",
			"}\n",

			"table.list td.orient-gay {\n",
			"  text-align: center;\n",
			"  background: #ffccff;\n",
			"  color: black;\n",
			"}\n",

			"table.list td.orient-bi {\n",
			"  text-align: center;\n",
			"  background: #ffffcc;\n",
			"  color: black;\n",
			"}\n",

			"table.list td.orient-straight {\n",
			"  text-align: center;\n",
			"  background: #ccffff;\n",
			"  color: black;\n",
			"}\n",

			"</style>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>T</th>\n",
			"<th>G</th>\n",
			"<th>O</th>\n",
			"<th>Name</th>\n",
			"<th>Pic</th>\n",
			"<th>Info</th>\n",
			"<th>Loc</th>\n",
			"<th>Idle</th>\n",
			"</tr>\n");

		for (
			ChatUserRec chatUser
				: users
		) {

			printFormat (
				"<tr",
				" class=\"magic-table-row\"",
				" data-target-href=\"%h\"",
				requestContext.resolveContextUrl (
					stringFormat (
						"%s",
						targetContext.pathPrefix (),
						"/%u",
						chatUser.getId ())),
				">\n");

			printFormat (
				"<td>%h</td>\n",
				chatUser.getCode ());

			printFormat (
				"%s\n",
				chatConsoleLogic.tdForChatUserTypeShort (
					chatUser));

			printFormat (
				"%s\n",
				chatConsoleLogic.tdForChatUserGenderShort (
					chatUser));

			printFormat (
				"%s\n",
				chatConsoleLogic.tdForChatUserOrientShort (
					chatUser));

			printFormat (
				"<td>%s</td>\n",
				Html.nonBreakingWhitespace (
					Html.encode (
						chatUser.getName ())));

			if (! chatUser.getChatUserImageList ().isEmpty ()) {

				printFormat (
					"<td>%s</td>\n",
					mediaConsoleLogic.mediaThumb32 (
						chatUser.getChatUserImageList ().get (0).getMedia ()));

			} else {

				printFormat (
					"<td>-</td>\n");

			}

			printFormat (
				"<td>%h</td>\n",
				chatUser.getInfoText () != null
					? spacify (
						chatUser.getInfoText ().getText ())
					: "");

			String placeName = "-";

			if (chatUser.getLocationLongLat () != null) {

				GazetteerEntryRec gazetteerEntry =
					gazetteerLogic.findNearestCanonicalEntry (
						chatUser.getChat ().getGazetteer (),
						chatUser.getLocationLongLat ());

				placeName =
					gazetteerEntry.getName ();

			}

			printFormat (
				"<td>%h</td>\n",
				placeName);

			if (chatUser.getLastAction () != null) {

				printFormat (
					"<td>%s</td>\n",
					Html.encodeNonBreakingWhitespace (
						timeFormatter.prettyDuration (
							chatUser.getLastAction (),
							transaction.now ())));

			} else {

				printFormat (
					"<td>-</td>\n");

			}

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
