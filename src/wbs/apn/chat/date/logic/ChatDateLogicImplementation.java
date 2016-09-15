package wbs.apn.chat.date.logic;

import static wbs.utils.etc.Misc.prettyHour;

import java.util.Collections;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserDateLogObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("chatDateLogic")
public
class ChatDateLogicImplementation
	implements ChatDateLogic {

	// singleton dependencies

	@SingletonDependency
	ChatUserDateLogObjectHelper chatUserDateLogHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	// implementation

	@Override
	public
	void chatUserDateJoinHint (
			@NonNull ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// send the message

		chatSendLogic.sendSystemMagic (
			chatUser,
			Optional.<Long>absent (),
			"date_hint_photo",
			commandHelper.findByCodeRequired (
				chat,
				"date_join_photo"),
			0l,
			TemplateMissing.error,
			Collections.<String,String>emptyMap ());

		// and update the chat user

		chatUser

			.setLastDateHint (
				transaction.now ());

	}

	@Override
	public
	void chatUserDateUpgradeHint (
			ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// send the message

		chatSendLogic.sendSystemMagic (
			chatUser,
			Optional.absent (),
			"date_hint_upgrade",
			commandHelper.findByCodeRequired (
				chat,
				"magic"),
			IdObject.objectId (
				commandHelper.findByCodeRequired (
					chat,
					"date_join_photo")),
			TemplateMissing.error,
			Collections.emptyMap ());

		// and update the chat user

		chatUser

			.setLastDateHint (
				transaction.now ());

	}

	@Override
	public
	void userDateStuff (
			ChatUserRec chatUser,
			UserRec user,
			MessageRec message,
			ChatUserDateMode dateMode,
			Long radius,
			Long startHour,
			Long endHour,
			Long dailyMax,
			boolean sendMessage) {

		Transaction transaction =
			database.currentTransaction ();

		if (chatUser == null)
			throw new NullPointerException ();

		ChatRec chat =
			chatUser.getChat ();

		if (
			(radius != null
				&& radius < 1)
			|| (startHour != null
				&& (startHour < 0 || startHour > 23))
			|| (radius != null
				&& (endHour < 0 || endHour > 23))
		) {

			throw new IllegalArgumentException (
				"Illegal radius, start hour or end hour");

		}

		if (
			(user != null)
			&& (message != null)
		) {

			throw new IllegalArgumentException (
				"Cannot specify both user and message.");

		}

		if (
			(dateMode == null
				|| chatUser.getDateMode () == dateMode)
			&& (radius == null
				|| chatUser.getDateRadius () == radius)
			&& (startHour == null
				|| chatUser.getDateStartHour () == startHour)
			&& (endHour == null
				|| chatUser.getDateEndHour () == endHour)
			&& (dailyMax == null
				|| chatUser.getDateDailyMax () == dailyMax)
		) {

			return;

		}

		chatUserHelper.lock (
			chatUser);

		if (dateMode != null)
			chatUser.setDateMode (dateMode);

		if (radius != null)
			chatUser.setDateRadius (radius);

		if (startHour != null)
			chatUser.setDateStartHour (startHour);

		if (endHour != null)
			chatUser.setDateEndHour (endHour);

		if (dailyMax != null)
			chatUser.setDateDailyMax (dailyMax);

		if (dateMode != null && dateMode != ChatUserDateMode.none)
			chatUser.setBlockAll (false);

		chatUserDateLogHelper.insert (
			chatUserDateLogHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setUser (
				user)

			.setMessage (
				message)

			.setDateMode (
				dateMode)

			.setRadius (
				chatUser.getDateRadius ())

			.setStartHour (
				chatUser.getDateStartHour ())

			.setEndHour (
				chatUser.getDateEndHour ())

			.setDailyMax (
				chatUser.getDateDailyMax ())

			.setTimestamp (
				transaction.now ())

		);

		if (sendMessage) {

			String code;

			switch (chatUser.getDateMode ()) {

			case none:
				code = "date_confirm_stop";
				break;

			case photo:
				code = "date_confirm_photo";
				break;

			case text:
				code = "date_confirm_text";
				break;

			default:
				throw new RuntimeException ();

			}

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				code,
				commandHelper.findByCodeRequired (
					chat,
					"magic"),
				IdObject.objectId (
					commandHelper.findByCodeRequired (
						chat,
						"help")),
				TemplateMissing.ignore,
				ImmutableMap.<String,String>builder ()

					.put (
						"miles",
						chatUser.getDateRadius ().toString ())

					.put (
						"start",
						prettyHour (
							chatUser.getDateStartHour ()))

					.put (
						"end",
						prettyHour (
							chatUser.getDateEndHour ()))

					.build ());

		}

		// set first join if appropriate

		if (
			chatUser.getFirstJoin () == null
			&& chatUser.getDateMode () != ChatUserDateMode.none
		) {

			chatUser

				.setFirstJoin (
					transaction.now ());

		}

	}

	@Override
	public
	void userDateStuff (
			ChatUserRec chatUser,
			UserRec user,
			MessageRec message,
			ChatUserDateMode dateMode,
			boolean sendMessage) {

		userDateStuff (
			chatUser,
			user,
			message,
			dateMode,
			chatUser.getDateRadius (),
			chatUser.getDateStartHour (),
			chatUser.getDateEndHour (),
			chatUser.getDateDailyMax (),
			sendMessage);

	}

}
