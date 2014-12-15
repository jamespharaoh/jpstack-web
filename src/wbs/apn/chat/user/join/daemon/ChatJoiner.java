package wbs.apn.chat.user.join.daemon;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.daemon.ChatPatterns;
import wbs.apn.chat.core.logic.ChatLogicHooks;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserDobFailureObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserDobFailureRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.locator.logic.LocatorManager;
import wbs.sms.locator.model.LongLat;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.inbox.daemon.CommandHandler.Status;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.daemon.ReceivedMessageImpl;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("chatJoiner")
public
class ChatJoiner {

	// dependencies

	@Inject
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatLogicHooks chatHooks;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserDobFailureObjectHelper chatUserDobFailureHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	LocatorManager locatorManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	Random random;

	@Inject
	ServiceObjectHelper serviceHelper;

	// properties

	@Getter @Setter
	Integer chatId;

	@Getter @Setter
	JoinType joinType;

	@Getter @Setter
	Gender gender;

	@Getter @Setter
	Orient orient;

	@Getter @Setter
	Integer chatAffiliateId;

	@Getter @Setter
	Integer chatSchemeId;

	@Getter @Setter
	boolean confirmCharges;

	boolean locatorEnabled = false;

	// state

	ChatRec chat;
	ChatUserRec chatUser;
	MessageRec message;
	ReceivedMessage receivedMessage;
	Integer deliveryId;
	DeliveryRec delivery;
	String rest;
	boolean gotPlace;
	int locatorId, numberId, serviceId, affiliateId; // locator input
	LongLat locatedLongLat; // locator output

	// implementation

	protected
	void sendMagicSystem (
			String templateCode,
			Record<?> commandParent,
			String commandCode,
			Map<String,String> params) {

		chatSendLogic.sendSystemMagic (
			chatUser,
			Optional.of (message.getThreadId ()),
			templateCode,
			commandHelper.findByCode (
				chat,
				"magic"),
			commandHelper.findByCode (
				commandParent,
				commandCode
			).getId (),
			params);

	}

	/**
	 * Checks the message for user prefs and updates the user's details with
	 * them.
	 */
	protected
	void updateUserPrefs () {

		boolean gay =
			ChatPatterns.gay.matcher (rest).find ();

		boolean straight =
			ChatPatterns.straight.matcher (rest).find ();

		boolean bi =
			ChatPatterns.bi.matcher (rest).find ();

		boolean male =
			ChatPatterns.male.matcher (rest).find ();

		boolean female =
			ChatPatterns.female.matcher (rest).find ();

		if (gay && ! straight && ! bi)
			chatUser.setOrient (Orient.gay);

		if (! gay && straight && !bi)
			chatUser.setOrient (Orient.straight);

		if (! gay && ! straight && bi)
			chatUser.setOrient (Orient.bi);

		if (male && ! female)
			chatUser.setGender (Gender.male);

		if (! male && female)
			chatUser.setGender (Gender.female);

	}

	protected
	void updateUserGender () {

		boolean male =
			ChatPatterns.male.matcher (rest).find ();

		boolean female =
			ChatPatterns.female.matcher (rest).find ();

		if (male && ! female)
			chatUser.setGender (Gender.male);

		if (! male && female)
			chatUser.setGender (Gender.female);

	}

	protected
	void updateUserGenderOther () {

		if (chatUser.getGender () == null)
			return;

		boolean male =
			ChatPatterns.male.matcher (rest).find ();

		boolean female =
			ChatPatterns.female.matcher (rest).find ();

		boolean both =
			ChatPatterns.both.matcher (rest).find ();

		if (male && ! female && ! both) {

			chatUser.setOrient (
				chatUser.getGender () == Gender.male
					? Orient.gay
					: Orient.straight);

		}

		if (! male && female && ! both) {

			chatUser.setOrient (
				chatUser.getGender () == Gender.male
					? Orient.straight
					: Orient.gay);

		}

		if ((male && female) || both) {

			chatUser.setOrient (Orient.bi);

		}

	}

	protected
	void updateUserDob () {

		LocalDate dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		if (dateOfBirth != null) {

			chatUser

				.setDob (
					dateOfBirth);

		} else {

			chatUserDobFailureHelper.insert (
				new ChatUserDobFailureRec ()

				.setChatUser (
					chatUser)

				.setMessage (
					message)

				.setTimestamp (
					new Date ())

				.setFailingText (
					message.getText ())

			);

		}

	}

	protected
	boolean updateUserPhoto () {

		ChatUserImageRec chatUserImage =
			chatUserLogic.setPhotoFromMessage (
				chatUser,
				message,
				false);

		if (chatUserImage == null) {

			chatSendLogic.sendSystemMmsFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"photo_error",
				commandHelper.findByCode (
					chatUser.getChatAffiliate (),
					"date_set_photo"));

			return false;

		}

		return true;

	}

	/**
	 * Save the appropriate information in this user.
	 */
	protected
	boolean updateUser () {

		if (gender != null)
			chatUser.setGender (gender);

		if (orient != null)
			chatUser.setOrient (orient);

		if (deliveryId != null) {
			chatUserLogic.adultVerify (chatUser);
			return true;
		}

		if (
			in (joinType,
				JoinType.chatPrefs)
		) {

			updateUserPrefs ();

		}

		// set gender

		if (
			in (joinType,
				JoinType.chatGender,
				JoinType.dateGender)
		) {

			updateUserGender ();

		}

		if (
			in (joinType,
				JoinType.chatGenderOther,
				JoinType.dateGenderOther)
		) {

			updateUserGenderOther ();

		}

		// set info

		if (
			in (joinType,
				JoinType.chatSetInfo,
				JoinType.dateSetInfo)
			&& rest.length () > 10
		) {

			chatInfoLogic.chatUserSetInfo (
				chatUser,
				rest,
				message.getThreadId ());

		}

		// check age

		if (
			in (joinType,
				JoinType.chatAge)
			&& ChatPatterns.yes.matcher (
				receivedMessage.getRest ()
			).find ()
		) {

			chatUser

				.setAgeChecked (
					true);

		}

		// confirm charges

		if (
			in (joinType,
				JoinType.chatCharges,
				JoinType.dateCharges)
			&& ChatPatterns.yes.matcher (
				receivedMessage.getRest ()
			).find ()
		) {

			chatUser

				.setChargesConfirmed (
					true);

		}

		if (confirmCharges) {

			chatUser

				.setChargesConfirmed (
					true);

		}

		// set date of birth

		if (
			in (joinType,
				JoinType.chatDob,
				JoinType.dateDob)
		) {

			updateUserDob ();

		}

		// set location

		if (
			in (joinType,
				JoinType.chatLocation,
				JoinType.dateLocation)
		) {

			gotPlace =
				chatUserLogic.setPlace (
					chatUser,
					rest,
					Optional.of (
						message));

		}

		// set affiliate

		if (chatAffiliateId != null) {

			chatUserLogic.setAffiliate (
				chatUser,
				chatAffiliateHelper.find (
					chatAffiliateId));

		}

		if (chatUser.getChatAffiliate () != null) {

			receivedMessage.setAffiliateId (
				chatUserLogic.getAffiliateId (chatUser));

		}

		// set the chat user's scheme if appropriate

		if (chatSchemeId != null) {

			chatUserLogic.setScheme (
				chatUser,
				chatSchemeHelper.find (chatSchemeId));

		}

		// set photo, if appropriate

		if (
			in (joinType,
				JoinType.dateSetPhoto)
		) {

			if (! updateUserPhoto ())
				return false;

		}

		return true;

	}

	/**
	 * Checks for various things which need to be set up before we can join.
	 * If everything is ok will do nothing and return true. If there is
	 * something missing will send a message to the user and return false.
	 *
	 * @param js
	 *            information about the current join.
	 * @return true to continue joining, false to abort immediately.
	 */
	private
	boolean checkBeforeJoin () {

		// check adult verification
		// if (js.receivedMessage != null
		// && (eq (chatSchemeId, 17)
		// || eq (js.chatUser.getChatScheme ().getId (), 17))
		// && js.chatUser.getNumber ().getNetwork ().getId () == 4
		// && js.message.getRoute ().getId () == 41
		// && ! js.chatUser.getAdultVerified ()) {
		//
		// smsUtils.sendMessage (
		// js.message.getId (),
		// js.message.getNumber (),
		// "This message confirms your number is adult verified. Thankyou.",
		// "84469",
		// smsDao.findRouteById (58),
		// js.chat.getService (),
		// null,
		// js.chatUser.getAffiliate (),
		// smsDao.findDeliveryNoticeTypeByCode ("chat_adult"),
		// js.chatUser.getId (),
		// null,
		// null,
		// true,
		// null,
		// null, null, null);
		// js.chatUser.setNextJoinType (
		// joinTypeIsChat (joinType)?
		// ChatKeywordJoinType.chatSimple :
		// ChatKeywordJoinType.dateSimple);
		// return false;
		// }

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			sendMagicSystem (
				"join_error",
				chat,
				"help",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check credit

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				message.getThreadId ());

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			return false;

		}

		// check age

		if (! chatUserLogic.gotDob (chatUser)) {

			if (chat.getSendDobRequestFromShortcode ()) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (message.getThreadId ()),
					"dob_request",
					Collections.<String,String>emptyMap ());

				chatUser

					.setNextJoinType (
						joinTypeIsChat (joinType)
							? ChatKeywordJoinType.chatDob
							: ChatKeywordJoinType.dateDob);

			} else {

				sendMagicSystem (
					"dob_request",
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_dob"
						: "date_dob",
					Collections.<String,String>emptyMap ());

			}

			return false;

		}

		if (! chatUserLogic.dobOk (chatUser)) {

			sendMagicSystem (
				"dob_too_young",
				chat,
				"help",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// send join warning

		if (
			chat.getJoinWarningEnabled ()
			&& ! chatUser.getJoinWarningSent ()
		) {

			if (chat.getSendWarningFromShortcode ()) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (message.getThreadId ()),
					"join_warning",
					Collections.<String,String>emptyMap ());

			} else {

				sendMagicSystem (
					"join_warning",
					chatUser.getChatScheme (),
					"help",
					Collections.<String,String>emptyMap ());

			}

			if (chat.getBillDuringJoin ()) {

				chatCreditLogic.userBillReal (
					chatUser,
					false);

			}

			chatUser

				.setJoinWarningSent (
					true);

		}

		// check charges confirmed

		if (! chatUser.getChargesConfirmed ()) {

			sendMagicSystem (
				"charges_request",
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_charges"
					: "date_charges",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check we got a decent location

		if (in (joinType,
				JoinType.chatLocation,
				JoinType.dateLocation)
			&& ! gotPlace) {

			sendMagicSystem (
				"location_error",
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_location"
					: "date_location",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check gender

		if (chatUser.getGender () == null) {

			sendMagicSystem (
				"gender_request",
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_gender"
					: "date_gender",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check orient

		if (chatUser.getOrient () == null) {

			sendMagicSystem (
				"gender_other_request",
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_gender_other"
					: "date_gender_other",
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check info

		if (chatUser.getInfoText () == null
				&& chatUser.getNewChatUserInfo () == null) {

			sendMagicSystem (
				"info_request",
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_info"
					: "date_info",
				Collections.<String,String>emptyMap ());

			return false;
		}

		return true;
	}

	boolean checkLocation () {

		// if we have a location that's fine
		if (chatUser.getLocLongLat () != null)
			return true;

		// otherwise, send an error

		sendMagicSystem (
			"location_request",
			chatUser.getChatScheme (),
			joinTypeIsChat (joinType)
				? "chat_location"
				: "date_location",
			Collections.<String,String>emptyMap ());

		return false;

	}

	public
	Status handle (
			ReceivedMessage receivedMessage,
			String rest) {

		return handle (
			new ReceivedMessageImpl (
				receivedMessage,
				rest));

	}

	public Status handle (
			ReceivedMessage receivedMessage) {

		// save stuff

		this.receivedMessage = receivedMessage;
		this.rest = receivedMessage.getRest ();

		// delegate to realSend

		return realSend ();

	}

	public
	void delivery (
			Integer deliveryIdId) {

		this.deliveryId =
			deliveryIdId;

		realSend ();

	}

	/**
	 * Multi-purpose join command.
	 */
	public
	Status realSend () {

		// TODO this could be much simpler

		if (! in (
				joinType,
				JoinType.chatLocation,
				JoinType.dateLocation)) {

			@Cleanup
			Transaction transaction1 =
				database.beginReadWrite ();

			// do part one

			if (! joinPart1 ()) {

				if (delivery != null) {

					deliveryHelper.remove (
						delivery);

				}

				transaction1.commit ();

				return Status.processed;

			}

			// if preLocator fails just do part two in the same
			// transaction

			if (! preLocator ()) {

				joinPart2 ();

				if (delivery != null) {

					deliveryHelper.remove (
						delivery);

				}

				transaction1.commit ();

				return Status.processed;
			}

			transaction1.commit ();

			// then do the location lookup (this takes a while so we do it
			// outside a transaction

			doLocator ();

			// do part two in a separate transaction

			@Cleanup
			Transaction transaction2 =
				database.beginReadWrite ();

			postLocator ();

			joinPart2 ();

			if (delivery != null) {

				deliveryHelper.remove (
					delivery);

			}

			transaction2.commit ();

		} else {

			// just do the whole join in one transaction

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			if (! joinPart1 ()) {

				if (delivery != null) {

					deliveryHelper.remove (
						delivery);

				}

				transaction.commit ();

				return Status.processed;
			}

			joinPart2 ();

			if (delivery != null) {

				deliveryHelper.remove (
					delivery);

			}

			transaction.commit ();

		}

		return Status.processed;

	}

	private
	boolean joinPart1 () {

		Transaction transaction =
			database.currentTransaction ();

		// lookup stuff

		chat =
			chatHelper.find (chatId);

		if (receivedMessage != null) {

			message =
				messageHelper.find (
					receivedMessage.getMessageId ());

			chatUser =
				chatUserHelper.findOrCreate (
					chat,
					message);

		} else {

			delivery =
				deliveryHelper.find (
					deliveryId);

			message =
				messageHelper.find (
					delivery.getMessage ().getThreadId ());

			chatUser =
				chatUserHelper.findOrCreate (
					chat,
					delivery.getMessage ());

		}

		// update received message stuff

		if (receivedMessage != null) {

			receivedMessage.setServiceId (
				serviceHelper.findByCode (chat, "default").getId ());

			Integer affiliateId =
				chatUserLogic.getAffiliateId (chatUser);

			if (affiliateId != null)
				receivedMessage.setAffiliateId (affiliateId);

		}

		// make sure the user can join

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				message.getThreadId ());

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			return false;

		}

		// schedule a register help message (we cancel this when the join is
		// successful)

		if (chatUser.getLastJoin () == null) {

			Instant nextRegisterHelpTime =
				transaction
					.now ()
					.plus (Duration.standardMinutes (20));

			chatUser

				.setNextRegisterHelp (
					instantToDate (
						nextRegisterHelpTime));

		}

		// set last action and schedule ad

		chatUser

			.setLastAction (
				instantToDate (
					transaction.now ()));

		chatUserLogic.scheduleAd (
			chatUser);

		// update the user as appropriate

		if (! updateUser ())
			return false;

		// do pre-join checks

		if (! checkBeforeJoin ())
			return false;

		return true;

	}

	private
	boolean joinPart2 () {

		Transaction transaction =
			database.currentTransaction ();

		log.debug (
			stringFormat (
				"Checking location for chat user %s",
				objectManager.objectPathMini (
					chatUser)));

		// check we have a location of some sort

		if (! checkLocation ())
			return false;

		log.debug (
			stringFormat (
				"Location ok for chat user %s (%s)",
				objectManager.objectPathMini (
					chatUser),
				chatUser.getLocLongLat ()));

		// bring the user online if appropriate

		if (joinTypeIsChat (joinType)) {

			chatMiscLogic.userJoin (
				chatUser,
				true,
				message.getThreadId (),
				ChatMessageMethod.sms);

		}

		// schedule next outbound message

		Instant nextQuietOutboundTime =
			transaction
				.now ()
				.plus (Duration.standardSeconds (
					chat.getTimeQuietOutbound ()));

		chatUser

			.setNextQuietOutbound (
				instantToDate (
					nextQuietOutboundTime));

		// schedule next join outbound message

		Instant nextJoinOutboundTime =
			transaction
				.now ()
				.plus (Duration.standardSeconds (
					+ chat.getTimeJoinOutboundMin ()
					+ random.nextInt (
						+ chat.getTimeJoinOutboundMax ()
						- chat.getTimeJoinOutboundMin ())));

		chatUser

			.setNextJoinOutbound (
				instantToDate (
					nextJoinOutboundTime));

		// bring them into dating if appropriate

		if (joinTypeIsDate (joinType)) {

			if (chatUser.getDateMode () == ChatUserDateMode.none) {

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (message.getThreadId ()),
					"date_joined",
					commandHelper.findByCode (chat, "help"),
					0,
					null);

			}

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				message,
				chatUser.getMainChatUserImage () != null
					? ChatUserDateMode.text
					: ChatUserDateMode.photo,
				true);

			if (chatUser.getDateDailyCount ()
					< chatUser.getDateDailyMax ()) {

				chatUser.setDateDailyCount (
					chatUser.getDateDailyMax ());

			}

		}

		// set session info remain

		if (
			joinType == JoinType.chatNext
			&& chat.getSessionInfoLimit () != null
		) {

			chatUser

				.setSessionInfoRemain (
					chat.getSessionInfoLimit ());

		}

		// send them the next users or pictures or whatever

		switch (joinType) {

		case chatPics:

			if (
				0 == chatInfoLogic.sendUserPics (
					chatUser,
					3,
					message.getThreadId ())
			) {

				sendMagicSystem (
					"more_photos_error",
					chat,
					"help",
					Collections.<String,String>emptyMap ());

			}

			break;

		case chatVideos:

			if (
				0 == chatInfoLogic.sendUserVideos (
					chatUser,
					3,
					message.getThreadId ())
			) {

				sendMagicSystem (
					"more_videos_error",
					chat,
					"help",
					Collections.<String,String>emptyMap ());

			}

			break;

		case chatNext:

			int numSent =
				chatInfoLogic.sendUserInfos (
					chatUser,
					2,
					message.getThreadId ());

			if (numSent > 1) {

				if (chatUser.getSessionInfoRemain () != null) {

					chatUser

						.setSessionInfoRemain (
							chatUser.getSessionInfoRemain () - numSent);

				}

			} else {

				sendMagicSystem (
					"more_error",
					chat,
					"help",
					Collections.<String,String>emptyMap ());

			}

			break;

		default:

			// do nothing

		}

		// send any queued message

		ChatMessageRec oldMessage =
			chatMessageHelper.findSignup (
				chatUser);

		if (oldMessage != null) {

			chatMessageLogic.chatMessageSendFromUserPartTwo (
				oldMessage);

		}

		// auto join chat and dating

		chatMiscLogic.userAutoJoin (
			chatUser,
			message);

		// run hooks

		chatHooks.chatUserSignupComplete (
			chatUser);

		return true;

	}

	private
	boolean preLocator () {

		Transaction transaction =
			database.currentTransaction ();

		// always return false - this disables the LBS service

		if (! locatorEnabled)
			return false;

		// if we have a location from within the last hour just use that

		if (

			chatUser.getLocTime () != null

			&& chatUser.getLocTime ().getTime ()
				>= transaction.now ().getMillis () - 60 * 60 * 1000

			&& chatUser.getLocLongLat () != null

		) {

			return false;

		}

		// update the chat user's loc time now

		chatUser

			.setLocTime (
				instantToDate (
					transaction.now ()));

		// get all the info to look up before closing the session

		locatorId =
			chat.getLocator ().getId ();

		numberId =
			message.getNumber ().getId ();

		serviceId =
			serviceHelper.findByCode (chat, "default").getId ();

		affiliateId =
			ifNull (
				chatUserLogic.getAffiliateId (chatUser),
				0);

		return true;

	}

	private
	void doLocator () {

		// do the location lookup

		try {

			locatedLongLat =
				locatorManager.locate (
					locatorId,
					numberId,
					serviceId,
					affiliateId);

		} catch (Throwable exception) {

			log.error (
				"Locator failed",
				exception);

		}

	}

	private
	void postLocator () {

		Transaction transaction =
			database.currentTransaction ();

		// lookup stuff

		chat =
			chatHelper.find (
				chatId);

		if (receivedMessage != null) {

			message =
				messageHelper.find (
					receivedMessage.getMessageId ());

			chatUser =
				chatUserHelper.findOrCreate (
					chat,
					message);

		} else {

			delivery =
				deliveryHelper.find (
					deliveryId);

			message =
				messageHelper.find (
					delivery.getMessage ().getThreadId ());

			chatUser =
				chatUserHelper.findOrCreate (
					chat,
					delivery.getMessage ());

		}

		// update the user if appropriate

		if (locatedLongLat != null) {

			chatUser

				.setLocLongLat (
					locatedLongLat)

				.setLocTime (
					instantToDate (
						transaction.now ()));

		}

	}

	public static
	enum JoinType {

		chatSimple,
		chatSetInfo,
		chatNext,
		chatLocation,
		chatPrefs,
		chatGender,
		chatGenderOther,
		chatDob,
		chatCharges,
		chatPics,
		chatVideos,
		chatAge,

		dateSimple,
		dateSetInfo,
		dateLocation,
		dateGender,
		dateGenderOther,
		dateDob,
		dateCharges,
		dateSetPhoto
	}

	private
	boolean joinTypeIsChat (
			@NonNull JoinType joinType) {

		switch (joinType) {

		case chatSimple: return true;
		case chatSetInfo: return true;
		case chatNext: return true;
		case chatLocation: return true;
		case chatPrefs: return true;
		case chatGender: return true;
		case chatGenderOther: return true;
		case chatDob: return true;
		case chatCharges: return true;
		case chatPics: return true;
		case chatVideos: return true;
		case chatAge: return true;

		case dateSimple: return false;
		case dateSetInfo: return false;
		case dateLocation: return false;
		case dateGender: return false;
		case dateGenderOther: return false;
		case dateDob: return false;
		case dateCharges: return false;
		case dateSetPhoto: return false;

		}

		throw new IllegalArgumentException (
			stringFormat (
				"Unrecognised join type: %s",
				joinType));

	}

	private
	boolean joinTypeIsDate (
			@NonNull JoinType joinType) {

		return ! joinTypeIsChat (
			joinType);

	}

	public static
	JoinType convertJoinType (
			@NonNull ChatKeywordJoinType chatKeywordJoinType) {

		switch (chatKeywordJoinType) {

			case chatSimple: return JoinType.chatSimple;
			case chatSetInfo: return JoinType.chatSetInfo;
			case chatNext: return JoinType.chatNext;
			case chatLocation: return JoinType.chatLocation;
			case chatDob: return JoinType.chatDob;
			case chatPics: return JoinType.chatPics;
			case chatVideos: return JoinType.chatVideos;
			case dateSimple: return JoinType.dateSimple;
			case dateSetInfo: return JoinType.dateSetInfo;
			case dateDob: return JoinType.dateDob;

			default:

				throw new RuntimeException (
					stringFormat (
						"Uknown keyword join type %s",
						chatKeywordJoinType));

		}

	}

}
