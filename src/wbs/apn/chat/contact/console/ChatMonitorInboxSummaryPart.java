package wbs.apn.chat.contact.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.LogicUtils.ifNotEmptyThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.ifPresentThenElse;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlInputUtils.htmlSelectYesNo;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteFormat;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

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

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.sms.gazetteer.logic.GazetteerLogic;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNamedNoteObjectHelper;
import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

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
	GazetteerLogic gazetteerLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlStyleBlockOpen ();

		htmlStyleRuleOpen (
			"span.namedNote form input");

		htmlStyleRuleEntryWrite (
			"width",
			"50%");

		htmlStyleRuleClose ();

		htmlStyleBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlTableOpenDetails ();

		// general details

		goParty ();
		goPrefs ();

		goCode (
			taskLogger);

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

		htmlTableClose ();

		goAdultVerified ();
		goNoAlarmWarning ();

		goHistory ();

	}

	void goSep () {

		htmlTableRowSeparatorWrite ();

	}

	void goAdultVerified () {

		if (userChatUser.getAdultVerified ()) {

			formatWriter.writeLineFormat (
				"<p>This user is adult verified</p>");

		} else {

			formatWriter.writeLineFormat (
				"<p style=\"background: #ff99cc;\">This user is NOT adult ",
				"verified. Please do not send them any adult content. ",
				"%h rejections.</p>",
				integerToDecimalString (
					userChatUser.getRejectionCount ()));

		}

	}

	void goNoAlarmWarning () {

		if (alarm == null) {

			formatWriter.writeLineFormat (
				"<p style=\"background: #99ffcc;\">This user does not have an ",
				"alarm set. You may wish to set one if they will expect a ",
				"response from you, even if they do not respond themself.");

		}

	}

	void goParty () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Party");

		formatWriter.writeLineFormat (
			"<td width=\"50%%\"><strong>monitor</strong></td>");

		formatWriter.writeLineFormat (
			"<td width=\"50%%\"><strong>user</strong></td>");

		htmlTableRowClose ();

	}

	void goPrefs () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Prefs");

		htmlTableCellWriteFormat (
			"%s %s (%s)",
			ifNotNullThenElseEmDash (
				monitorChatUser.getOrient (),
				() -> enumName (
					monitorChatUser.getOrient ())),
			ifNotNullThenElseEmDash (
				monitorChatUser.getGender (),
				() -> enumName (
					monitorChatUser.getGender ())),
			ifNotNullThenElseEmDash (
				monitorChatUser.getCategory (),
				() -> monitorChatUser.getCategory ().getName ()));

		htmlTableCellWriteFormat (
			"%s %s (%s)",
			ifNotNullThenElseEmDash (
				userChatUser.getOrient (),
				() -> enumName (
					userChatUser.getOrient ())),
			ifNotNullThenElseEmDash (
				userChatUser.getGender (),
				() -> enumName (
					userChatUser.getOrient ())),
			ifNotNullThenElseEmDash (
				userChatUser.getCategory (),
				() -> userChatUser.getCategory ().getName ()));

		htmlTableRowClose ();

	}

	void goCode (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goCode");

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"User number");

		objectManager.writeTdForObjectMiniLink (
			taskLogger,
			monitorChatUser,
			chat);

		objectManager.writeTdForObjectMiniLink (
			taskLogger,
			userChatUser,
			chat);

		htmlTableRowClose ();

	}

	void goName () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Name");

		htmlTableCellWrite (
			ifNullThenEmDash (
				monitorChatUser.getName ()));

		htmlTableCellWrite (
			ifNullThenEmDash (
				userChatUser.getName ()));

		htmlTableRowClose ();

	}

	void goInfo () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Info");

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				monitorChatUser.getInfoText (),
				() -> monitorChatUser.getInfoText ().getText ()));

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				userChatUser.getInfoText (),
				() -> userChatUser.getInfoText ().getText ()));

		htmlTableRowClose ();

	}

	void goPic () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Pic");

		htmlTableCellWriteHtml (
			ifNotEmptyThenElse (
				monitorChatUser.getChatUserImageList (),

			() ->
				mediaConsoleLogic.writeMediaThumb100 (
					formatWriter,
					monitorChatUser.getChatUserImageList ().get (0)
						.getMedia ()),

			() -> formatWriter.writeFormat (
				"—")

		));

		htmlTableCellWriteHtml (
			ifNotEmptyThenElse (
				userChatUser.getChatUserImageList (),

			() ->
				mediaConsoleLogic.writeMediaThumb100 (
					formatWriter,
					userChatUser.getChatUserImageList ().get (0).getMedia ()),

			() -> formatWriter.writeFormat (
				"—")

		));

		htmlTableRowClose ();

	}

	void goLocation () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Location");

		htmlTableCellWriteHtml (
			ifNotNullThenElseEmDash (
				monitorChatUser.getLocationLongLat (),
				() ->
					gazetteerLogic.findNearestCanonicalEntry (
						monitorChatUser.getChat ().getGazetteer (),
						monitorChatUser.getLocationLongLat ()
					).getName ()));

		htmlTableCellWriteHtml (
			ifNotNullThenElseEmDash (
				userChatUser.getLocationLongLat (),
				() ->
					gazetteerLogic.findNearestCanonicalEntry (
						userChatUser.getChat ().getGazetteer (),
						userChatUser.getLocationLongLat ()
					).getName ()));

		htmlTableRowClose ();

	}

	void goDob () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Date of birth");

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				monitorChatUser.getDob (),
				() -> stringFormat (
					"%s (%s)",
					timeFormatter.dateString (
						monitorChatUser.getDob ()),
					integerToDecimalString (
						chatUserLogic.getAgeInYears (
							monitorChatUser,
							now)))));

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				userChatUser.getDob (),
				() -> stringFormat (
					"%s (%s)",
					timeFormatter.dateString (
						userChatUser.getDob ()),
					integerToDecimalString (
						chatUserLogic.getAgeInYears (
							userChatUser,
							now)))));

		htmlTableRowClose ();

	}

	void goScheme () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Scheme");

		formatWriter.writeLineFormat (
			"<td colspan=\"2\" style=\"%h\">%h (%h)</td>",
			userChatUser.getChatScheme ().getStyle (),
			userChatUser.getChatScheme ().getCode (),
			userChatUser.getChatScheme ().getRbNumber ());

		htmlTableRowClose ();

	}

	void goNotesHeader () {

		htmlTableRowOpen ();

		formatWriter.writeLineFormat (
			"<th",
			" colspan=\"3\"",
			" style=\"font-size: 120%%\"",
			">Notes (%s, %s)</th>",

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
				">general notes</a>")

		);

		htmlTableRowClose ();

	}

	void goNamedNotes () {

		for (
			ChatNoteNameRec chatNoteName
				: chatNoteNames
		) {

			formatWriter.writeLineFormat (
				"<tr",
				" class=\"namedNoteRow\"",
				" style=\"display: none\"",
				">");

			htmlTableHeaderCellWrite (
				chatNoteName.getName ());

			htmlTableCellWriteHtml (
				goNamedNote (
					chatNoteName,
					userNamedNotes.get (
						chatNoteName.getId ()),
					"user"));

			htmlTableCellWriteHtml (
				goNamedNote (
					chatNoteName,
					monitorNamedNotes.get (chatNoteName.getId ()),
					"monitor"));

			htmlTableRowClose ();

		}

	}

	String goNamedNote (
			ChatNoteNameRec noteName,
			ChatNamedNoteRec namedNote,
			String type) {

		String key =
			stringFormat (
				"namedNote%s%s",
				integerToDecimalString (
					noteName.getId ()),
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

			formatWriter.writeLineFormat (
				"<tr",
				" class=\"%h\"",
				note.getPegged ()
					? "peggedNoteRow"
					: "unpeggedNoteRow",
				" style=\"display: none\"",
				">");

			htmlTableHeaderCellWrite (
				"Other");;

			htmlTableCellWrite (
				note.getNotes ());

			htmlTableCellOpen ();

			formatWriter.writeLineFormat (
				"<form",
				" method=\"post\"",
				" action=\"chatMonitorInbox.updateNote\"",
				">");

			formatWriter.increaseIndent ();

			formatWriter.writeLineFormat (
				"%h",
				noteInfo);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"id\"",
				" value=\"%h\"",
				integerToDecimalString (
					note.getId ()),
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"deleteNote\"",
				" value=\"delete\"",
				">");

			formatWriter.writeLineFormat (
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
				">");

			formatWriter.decreaseIndent ();

			formatWriter.writeFormat (
				"</form>");

			htmlTableRowClose ();

		}

	}

	void goAddNote () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Add");

		htmlTableCellOpen (
			htmlColumnSpanAttribute (2l));

		formatWriter.writeLineFormat (
			"<form",
			" method=\"post\"",
			" action=\"chatMonitorInbox.addNote\"",
			">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"moreNotes\"",
			" size=\"35\"",
			" value=\"\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"addNote\"",
			" value=\"add\"",
			">");

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</form>");

		htmlTableRowClose ();

	}

	void goAlarms () {

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Alarm time");

		formatWriter.writeLineFormat (
			"<td colspan=\"2\">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<form",
			" method=\"post\"",
			" action=\"chatMonitorInbox.alarm\"",
			">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
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
			">");

		formatWriter.writeLineFormat (
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
			">");

		htmlSelectYesNo (
			"alarmSticky",
			ifPresentThenElse (
				requestContext.parameter (
					"alarmSticky"),
				() -> Boolean.parseBoolean (
					requestContext.parameterRequired (
						"alarmSticky")),
				() -> ifNotNullThenElse (
					alarm,
					() -> alarm.getSticky (),
					() -> true)),
			"sticky",
			"not sticky");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"alarmSet\"",
			" value=\"set\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"alarmCancel\"",
			" value=\"cancel\"",
			">");

		formatWriter.writeLineFormat (
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

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</form>");

		htmlTableCellClose ();

		htmlTableRowClose ();

	}

	void goHistory () {

		htmlHeadingTwoWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			"Message",
			"User");

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

				htmlTableRowSeparatorWrite ();

				formatWriter.writeLineFormat (
					"<tr style=\"font-weight: bold\">");

				formatWriter.increaseIndent ();

				formatWriter.writeLineFormat (
					"<td colspan=\"3\">%h</td>",
					timeFormatter.dateStringLong (
						chatTimezone,
						chatMessage.getTimestamp ()));

				formatWriter.decreaseIndent ();

				htmlTableRowClose ();

			}

			String rowClass =
				chatMessage.getFromUser () == userChatUser
					? "message-in"
					: "message-out";

			formatWriter.writeFormat (
				"<tr class=\"%h\">",
				rowClass);

			formatWriter.increaseIndent ();

			htmlTableCellWrite (
				timeFormatter.timeString (
					chatTimezone,
					chatMessage.getTimestamp ()));

			htmlTableCellWrite (
				ifNotNullThenElse (
					chatMessage.getEditedText (),
					() -> chatMessage.getEditedText ().getText (),
					() -> chatMessage.getOriginalText ().getText ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					enumEqualSafe (
						chatMessage.getFromUser ().getType (),
						ChatUserType.monitor)
					&& isNotNull (
						chatMessage.getSender ()),
					() -> chatMessage.getSender ().getUsername ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
