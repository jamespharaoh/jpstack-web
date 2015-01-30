package wbs.apn.chat.adult.daemon;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.delivery.model.DeliveryTypeRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@PrototypeComponent ("chatAdultDeliveryHandler")
public
class ChatAdultDeliveryHandler
	implements DeliveryHandler {

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	Provider<ChatJoiner> joinerProvider;

	@Override
	public
	void handle (
			int deliveryId,
			Integer ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		DeliveryTypeRec deliveryType =
			message.getDeliveryType ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				delivery.getMessage ().getRef ());

		// work out if it is a join

		boolean join;

		if (equal (
				deliveryType.getCode (),
				"chat_adult")) {

			join = false;

		} else if (equal (
				deliveryType.getCode (),
				"chat_adult_join")) {

			join = true;

		} else {

			throw new RuntimeException (
				deliveryType.getCode ());

		}

		// ensure we are going to a successful delivery

		if (delivery.getOldMessageStatus ().isGoodType ()
				|| ! delivery.getNewMessageStatus ().isGoodType ()) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// find and update the chat user

		chatUserLogic.adultVerify (
			chatUser);

		// stop now if we are joining but there is no join type saved

		if (join
				&& chatUser.getNextJoinType () == null) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// send a confirmation message if we are not joining

		if (! join) {

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (delivery.getMessage ().getThreadId ()),
				"adult_confirm",
				Collections.<String,String>emptyMap ());

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// joins are handled by the big nasty join command handler

		JoinType joinType =
			ChatJoiner.convertJoinType (
				chatUser.getNextJoinType ());

		int chatId =
			chatUser.getChat ().getId ();

		joinerProvider.get ()

			.chatId (
				chatId)

			.joinType (
				joinType)

			.handle ();

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"chat_adult",
			"chat_adult_join");

	}

}
