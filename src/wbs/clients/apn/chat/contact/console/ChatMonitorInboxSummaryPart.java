package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalIf;
import static wbs.framework.utils.etc.OptionalUtils.presentInstances;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.joinWithSemicolonAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localDateNotEqual;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.NonNull;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactNoteRec;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;

@PrototypeComponent ("chatMonitorInboxSummaryPart")
public
class ChatMonitorInboxSummaryPart
	extends AbstractPagePart {

	// singleont dependencies

	@SingletonDependency
	ChatContactNoteObjectHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatNamedNoteObjectHelper chatNamedNoteHelper;

	@SingletonDependency
	ChatNoteNameObjectHelper chatNoteNameHelper;

	@SingletonDependency
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	GazetteerLogic gazetteerLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	DateTimeZone chatTimezone;
	Instant now;

	ChatMonitorInboxRec monitorInbox;
	ChatRec chat;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	List <ChatMessageRec> chatMessageHistory;
	ChatUserAlarmRec alarm;
	List <ChatContactNoteRec> notes;
	List <ChatNoteNameRec> chatNoteNames;

	Map <Long, ChatNamedNoteRec> userNamedNotes;
	Map <Long, ChatNamedNoteRec> monitorNamedNotes;

	// implementation

	final static
	String timestampPattern =
		"yyyy'-'MM'-'dd HH':'mm'";

	static
	class ChatMessageComparator
		implements Comparator <ChatMessageRec> {

		@Override
		public
		int compare (
				@NonNull ChatMessageRec left,
				@NonNull ChatMessageRec right) {

			return new CompareToBuilder ()

				.append (
					left.getTimestamp (),
					right.getTimestamp ())

				.toComparison ();

		}

		static
		Comparator <ChatMessageRec> ascending =
			new ChatMessageComparator ();

		static
		Comparator <ChatMessageRec> descending =
			Collections.reverseOrder (
				ascending);

	}

	// TODO: Limit the history items here!!!! *NA*

	@Override
	public
	void prepare () {

		Transaction transaction =
			database.currentTransaction ();

		now =
			transaction.now ();

		monitorInbox =
			chatMonitorInboxHelper.findRequired (
				requestContext.stuffInteger (
					"chatMonitorInboxId"));

		monitorChatUser =
			monitorInbox.getMonitorChatUser ();

		userChatUser =
			monitorInbox.getUserChatUser ();

		ChatSchemeRec chatScheme =
			userChatUser.getChatScheme ();

		chatTimezone =
			DateTimeZone.forID (
				chatScheme.getTimezone ());

		chat =
			userChatUser.getChat ();

		chatMessageHistory =
			Lists.newArrayList (
				Iterables.concat (

			chatMessageHelper.findLimit (
				userChatUser,
				monitorChatUser,
				50l),

			chatMessageHelper.findLimit (
				monitorChatUser,
				userChatUser,
				50l)

		)).stream ()

			.filter (
				chatMessage ->
					enumNotInSafe (
						chatMessage.getStatus (),
						ChatMessageStatus.moderatorPending,
						ChatMessageStatus.moderatorRejected))

			.sorted (
				ChatMessageComparator.descending)

			.limit (
				50l)

			.collect (
				Collectors.toList ());

		alarm =
			chatUserAlarmHelper.find (
				userChatUser,
				monitorChatUser);

		// find notes

		notes =
			chatContactNoteHelper.find (
				userChatUser,
				monitorChatUser);

		chatNoteNames =
			chatNoteNameHelper.findNotDeleted (
				chat);

		userNamedNotes =
			getNamedNotes (
				userChatUser,
				monitorChatUser);

		monitorNamedNotes =
			getNamedNotes (
				monitorChatUser,
				userChatUser);

	}

	Map <Long, ChatNamedNoteRec> getNamedNotes (
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec thatUser) {

		Map <Long, ChatNamedNoteRec> namedNotes =
			new HashMap<> ();

		List <ChatNamedNoteRec> namedNotesTemp =
			chatNamedNoteHelper.find (
				thisUser,
				thatUser);

		for (
			ChatNamedNoteRec namedNote
				: namedNotesTemp
		) {

			namedNotes.put (
				namedNote.getChatNoteName ().getId (),
				namedNote);

		}

		return namedNotes;

	}

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				JqueryEditableScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/chat-monitor-inbox-summary.js"))

			.build ();

	}

	@Override
	public
	void renderHtmlHeadContent () {

		printFormat (
			"<style type=\"text/css\">\n",
			"span.namedNote form input {\n",
			"width: 50%%;\n",
			"}\n",
			"</style>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		// general details

		goParty ();
		goPrefs ();
		goCode ();
		goName ();
		goInfo ();
		goPic ();
		goLocation ();
		goDob ();
		goScheme ();

		// notes

		goSep ();
		goNotesHeader ();

		goSep ();
		goNamedNotes ();

		goSep ();
		goGeneralNotes ();

		goSep ();
		goAddNote ();

		goSep ();
		goAlarms ();

		printFormat (
			"</table>\n");

		goAdultVerified ();
		goNoAlarmWarning ();

		goHistory ();

	}

	void goSep () {

		printFormat (
			"<tr class=\"sep\">\n");

	}

	void goAdultVerified () {

		if (userChatUser.getAdultVerified ()) {

			printFormat (
				"<p>This user is adult verified</p>\n");

		} else {

			printFormat (
				"<p style=\"background: #ff99cc;\">This user is NOT adult ",
				"verified. Please do not send them any adult content. ",
				"%h rejections.</p>\n",
				userChatUser.getRejectionCount ());

		}

	}

	void goNoAlarmWarning () {

		if (alarm == null) {

			printFormat (
				"<p style=\"background: #99ffcc;\">This user does not have an ",
				"alarm set. You may wish to set one if they will expect a ",
				"response from you, even if they do not respond themself.\n");

		}

	}

	void goParty () {

		printFormat (
			"<tr>\n",

			"<th>Party</th>\n",

			"<td width=\"50%%\"><strong>monitor</strong></td>\n",

			"<td width=\"50%%\"><strong>user</strong></td>\n",

			"</tr>\n");
	}

	void goPrefs () {

		printFormat (
			"<tr>\n",
			"<th>Prefs</th>\n",

			"<td>%h %h (%h)</td>\n",
			ifNull (monitorChatUser.getOrient (), "unknown"),
			ifNull (monitorChatUser.getGender (), "something"),
			monitorChatUser.getCategory () != null
				? monitorChatUser.getCategory ().getName ()
				: "no category",

			"<td>%h %h (%h)</td>\n",
			ifNull (userChatUser.getOrient (), "unknown"),
			ifNull (userChatUser.getGender (), "something"),
			userChatUser.getCategory () != null
				? userChatUser.getCategory ().getName ()
				: "no category",

			"</tr>\n");

	}

	void goCode () {

		printFormat (
			"<tr>\n",
			"<th>User number</th>\n",

			"%s",
			objectManager.tdForObjectMiniLink (
				monitorChatUser,
				chat),

			"%s",
			objectManager.tdForObjectMiniLink (
				userChatUser,
				chat),

			"</tr>\n");

	}

	void goName () {

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td>%h</td>\n",
			ifNull (monitorChatUser.getName (), "-"),

			"<td>%h</td>\n",
			ifNull (userChatUser.getName (), "-"),

			"</tr>\n");
	}

	void goInfo () {

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n",

			"<td>%h</td>\n",
			ifNull (monitorChatUser.getInfoText (), "-"),

			"<td>%h</td>\n",
			ifNull (userChatUser.getInfoText (), "-"),

			"</tr>\n");
	}

	void goPic () {

		printFormat (
			"<tr>\n",
			"<th>Pic</th>");

		printFormat (
			"<td>%s</td>\n",
			monitorChatUser.getChatUserImageList ().isEmpty ()
				? "-"
				: mediaConsoleLogic.mediaThumb100 (
					monitorChatUser.getChatUserImageList ().get (0)
						.getMedia ()));

		printFormat (
			"<td>%s</td>\n",
			userChatUser.getChatUserImageList ().isEmpty ()
				? "-"
				: mediaConsoleLogic.mediaThumb100 (
					userChatUser.getChatUserImageList ().get (0).getMedia ()));

		printFormat (
			"</tr>\n");

	}

	void goLocation () {

		printFormat (
			"<tr>\n",
			"<th>Location</th>\n");

		printFormat (
			"<td>%s</td>\n",
			monitorChatUser.getLocationLongLat() != null
				? gazetteerLogic.findNearestCanonicalEntry (
						monitorChatUser.getChat ().getGazetteer (),
						monitorChatUser.getLocationLongLat ()
					).getName ()
				: "-");

		printFormat (
			"<td>%s</td>\n",
			userChatUser.getLocationLongLat() != null
				? gazetteerLogic.findNearestCanonicalEntry (
						userChatUser.getChat ().getGazetteer (),
						userChatUser.getLocationLongLat ()
					).getName ()
				: "-");

		printFormat (
			"</tr>\n");

	}

	void goDob () {

		printFormat (
			"<tr>\n",
			"<th>Date of birth</th>\n");

		if (
			isNotNull (
				monitorChatUser.getDob ())
		) {

			printFormat (
				"<td>%h (%h)</td>\n",
				timeFormatter.dateString (
					monitorChatUser.getDob ()),
				chatUserLogic.getAgeInYears (
					monitorChatUser,
					now));

		} else {

			printFormat (
				"<td>-</td>\n");

		}

		if (
			isNotNull (
				userChatUser.getDob ())
		) {

			printFormat (
				"<td>%h (%h)</td>\n",
				timeFormatter.dateString (
					userChatUser.getDob ()),
				chatUserLogic.getAgeInYears (
					userChatUser,
					now));

		} else {

			printFormat (
				"<td>-</td>\n");

		}

		printFormat (
			"</tr>\n");

	}

	void goScheme () {

		printFormat (
			"<tr>\n",
			"<th>Scheme</th>\n");

		printFormat (
			"<td colspan=\"2\" style=\"%h\">%h (%h)</td>\n",
			userChatUser.getChatScheme ().getStyle (),
			userChatUser.getChatScheme ().getCode (),
			userChatUser.getChatScheme ().getRbNumber ());

		printFormat (
			"</tr>\n");

	}

	void goNotesHeader () {

		printFormat (
			"<tr>\n");

		printFormat (
			"<th",
			" colspan=\"3\"",
			" style=\"font-size: 120%%\"",
			">Notes (%s, %s)</th>\n",

			stringFormat (
				"<a",
				" href=\"javascript:void(0)\"",
				" style=\"color: white\"",
				" class=\"namedNotesShowHideLink\"",
				">named notes</a>"),

			stringFormat (
				"<a",
				" href=\"javascript:void(0)\"",
				" style=\"color: white\"",
				" class=\"generalNotesShowHideLink\"",
				">general notes</a>"));

		printFormat (
			"</tr>\n");

	}

	void goNamedNotes () {

		for (
			ChatNoteNameRec chatNoteName
				: chatNoteNames
		) {

			printFormat (
				"<tr",
				" class=\"namedNoteRow\"",
				" style=\"display: none\"",
				">\n",

				"<th>%h</th>\n",
				chatNoteName.getName (),

				"<td>%s</td>\n",
				goNamedNote (
					chatNoteName,
					userNamedNotes.get (chatNoteName.getId ()),
					"user"),

				"<td>%s</td>\n",
				goNamedNote (
					chatNoteName,
					monitorNamedNotes.get (chatNoteName.getId ()),
					"monitor"),

				"</tr>\n");

		}

	}

	String goNamedNote (
			ChatNoteNameRec noteName,
			ChatNamedNoteRec namedNote,
			String type) {

		String key =
			stringFormat (
				"namedNote%d%s",
				noteName.getId (),
				type);

		return namedNote != null
				&& namedNote.getText () != null

			? stringFormat (

				"%s",
				stringFormat (
					"<span",
					" id=\"%h\"",
					key,
					" class=\"namedNote\"",
					">%h</span>",
					namedNote.getText ().getText ()),

				"&mdash;",

				"%s",
				stringFormat (
					"<span",
					" style=\"color: dimGrey\"",
					">%h, %h</span>",
					namedNote
						.getUser ()
						.getUsername (),
					namedNote
						.getTimestamp ()
						.toDateTime ()
						.toString (timestampPattern)))

			: stringFormat (
				"<span",
				" id=\"%h\"",
				key,
				" class=\"namedNote\"",
				"></span>");

	}

	void goGeneralNotes () {

		for (
			ChatContactNoteRec note
				: notes
		) {

			String noteInfo =
				joinWithCommaAndSpace (
					presentInstances (

				optionalIf (
					isNotNull (
						note.getConsoleUser ()),
					() ->
						note.getConsoleUser ().getUsername ()),

				optionalIf (
					isNotNull (
						note.getTimestamp ()),
					() ->
						timeFormatter.timestampTimezoneString (
							chatTimezone,
							note.getTimestamp ()))

			));

			printFormat (
				"<tr",
				" class=\"%h\"",
				note.getPegged ()
					? "peggedNoteRow"
					: "unpeggedNoteRow",
				" style=\"display: none\"",
				">\n",

				"<th>Other</th>\n",

				"<td>%h</td>\n",
				note.getNotes (),

				"<td>%s</td>\n",

				stringFormat (
					"<form",
					" method=\"post\"",
					" action=\"chatMonitorInbox.updateNote\"",
					">\n",

					"%h ",
					noteInfo,

					"<input",
					" type=\"hidden\"",
					" name=\"id\"",
					" value=\"%h\"",
					note.getId (),
					">\n",

					"<input",
					" type=\"submit\"",
					" name=\"deleteNote\"",
					" value=\"delete\"",
					">\n",

					"<input",
					" type=\"submit\"",
					" name=\"%h\"",
					note.getPegged ()
						? "unpegNote"
						: "pegNote",
					" value=\"%h\"",
					note.getPegged ()
						? "unpeg"
						: "peg",
					">\n",

					"</form>"),

				"</tr>\n");

		}

	}

	void goAddNote () {

		printFormat (
			"<tr>\n",

			"<th>Add</th>\n",

			"<td colspan='2'>%s</td>\n",

			stringFormat (
				"<form",
				" method=\"post\"",
				" action=\"chatMonitorInbox.addNote\"",
				">",

				"<input",
				" type=\"text\"",
				" name=\"moreNotes\"",
				" size=\"35\"",
				" value=\"\"",
				">",

				"<input",
				" type=\"submit\"",
				" name=\"addNote\"",
				" value=\"add\"",
				">",

				"</form>"),

			"</tr>\n");
	}

	void goAlarms () {

		printFormat (
			"<tr>\n");

		printFormat (
			"<th>Alarm time</th>\n");

		printFormat (
			"<td colspan=\"2\">\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"chatMonitorInbox.alarm\"",
			">\n");

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"alarmDate\"",
			" size=\"12\"",
			" value=\"%h\"",
			requestContext.parameterOrElse (
				"alarmDate",
				() -> timeFormatter.dateStringShort (
					chatTimezone,
					alarm == null
						? now
						: alarm.getAlarmTime ())),
			">\n");

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"alarmTime\"",
			" size=\"10\"",
			" value=\"%h\"",
			requestContext.parameterOrElse (
				"alarmTime",
				() -> timeFormatter.timeString (
					chatTimezone,
					alarm == null
						? now
						: alarm.getAlarmTime ())),
			">\n");

		printFormat (
			"%s",
			Html.selectYesNo (
				"alarmSticky",
				requestContext.parameterOrNull ("alarmSticky") != null
					? Boolean.parseBoolean (
						requestContext.parameterOrNull ("alarmSticky"))
					: alarm != null
						? alarm.getSticky ()
						: true,
				"sticky",
				"not sticky"));

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"alarmSet\"",
			" value=\"set\"",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"alarmCancel\"",
			" value=\"cancel\"",
			">\n");

		printFormat (
			"%s",
			alarm == null
				? "not set"
				: stringFormat (

					"<span",

					" style=\"%h\"",
					joinWithSemicolonAndSpace (
						"background: red",
						"color: white",
						"font-weight: bold",
						"padding: 2px 10px"),

					">set for %h, %h</span>",
					timeFormatter.timestampTimezoneString (
						chatTimezone,
						alarm.getAlarmTime ()),
					alarm.getSticky ()
						? "sticky"
						: "not sticky"));

		printFormat (
			"</form>\n");

		printFormat (
			"</td>",
			"</tr>\n");

	}

	void goHistory () {

		printFormat (
			"<h2>History</h2>");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Message</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		LocalDate previousDate = null;

		for (
			ChatMessageRec chatMessage
				: chatMessageHistory
		) {

			// ignore unapproved messages

			LocalDate newDate =
				chatMessage.getTimestamp ()

				.toDateTime (
					chatTimezone)

				.toLocalDate ();

			if (

				isNull (
					previousDate)

				|| localDateNotEqual (
					previousDate,
					newDate)

			) {

				previousDate =
					newDate;

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"3\">%h</td>\n",
					timeFormatter.dateStringLong (
						chatTimezone,
						chatMessage.getTimestamp ()));

			}

			String rowClass =
				chatMessage.getFromUser () == userChatUser
					? "message-in"
					: "message-out";

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.timeString (
					chatTimezone,
					chatMessage.getTimestamp ()));

			printFormat (
				"<td>%h</td>\n",
				ifNull (
					chatMessage.getEditedText (),
					chatMessage.getOriginalText ()).getText());

			if (chatMessage.getFromUser ().getType () == ChatUserType.monitor
					&& chatMessage.getSender () != null) {

				printFormat (
					"<td>%h</td>\n",
					chatMessage.getSender ().getUsername ());

			} else {

				printFormat (
					"<td>&nbsp;</td>\n");

			}

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
