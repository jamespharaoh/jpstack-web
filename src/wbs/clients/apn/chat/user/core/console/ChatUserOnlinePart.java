package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

@PrototypeComponent ("chatUserOnlinePart")
public
class ChatUserOnlinePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	GazetteerLogic gazetteerLogic;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	// state

	Collection<ChatUserRec> users;

	ConsoleContextType targetContextType;
	ConsoleContext targetContext;

	// implementation

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		users =
			new TreeSet<ChatUserRec> (
				ChatUserOnlineComparator.INSTANCE);

		users.addAll (
			chatUserHelper.findOnline (
				chat));

		targetContextType =
			consoleManager.contextType (
				"chatUser+",
				true);

		targetContext =
			consoleManager.relatedContext (
				requestContext.consoleContext (),
				targetContextType);

	}

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style type=\"text/css\">\n",
			"table.list td.chat-user-type-user { text-align: center; background: #cccccc; color: black; }\n",
			"table.list td.chat-user-type-monitor { text-align: center; background: #999999; color: white; }\n",
			"table.list td.gender-male { text-align: center; background: #ccccff; color: black; }\n",
			"table.list td.gender-female { text-align: center; background: #ffcccc; color: black; }\n",
			"table.list td.orient-gay { text-align: center; background: #ffccff; color: black; }\n",
			"table.list td.orient-bi { text-align: center; background: #ffffcc; color: black; \n",
			"table.list td.orient-straight { text-align: center; background: #ccffff; color: black; }\n",
			"</style>\n");

	}

	@Override
	public
	void goBodyStuff () {

		Date now =
			new Date ();

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

		for (ChatUserRec chatUser
				: users) {

			printFormat (
				"%s",
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
				chatConsoleLogic.tdForChatUserTypeShort (chatUser),

				"%s\n",
				chatConsoleLogic.tdForChatUserGenderShort (chatUser),

				"%s\n",
				chatConsoleLogic.tdForChatUserOrientShort (chatUser),

				"<td>%s</td>\n",
				Html.nbsp (
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
					? spacify (chatUser.getInfoText ().getText ())
					: "");

			String placeName = "-";

			if (chatUser.getLocLongLat () != null) {

				GazetteerEntryRec gazetteerEntry =
					gazetteerLogic.findNearestCanonicalEntry (
						chatUser.getChat ().getGazetteer (),
						chatUser.getLocLongLat ());

				placeName =
					gazetteerEntry.getName ();

			}

			printFormat (
				"<td>%h</td>\n",
				placeName);

			if (chatUser.getLastAction () != null) {

				printFormat (
					"<td>%s</td>\n",
					Html.nbsp (
						Html.encode (
							requestContext.prettyDateDiff (
								chatUser.getLastAction (),
								now))));

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
