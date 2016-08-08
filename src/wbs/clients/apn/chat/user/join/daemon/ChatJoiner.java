package wbs.clients.apn.chat.user.join.daemon;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.daemon.ChatPatterns;
import wbs.clients.apn.chat.core.logic.ChatLogicHooks;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.date.logic.ChatDateLogic;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.clients.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserDobFailureObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.EmailLogic;
import wbs.framework.utils.RandomLogic;
import wbs.framework.utils.TimeFormatter;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

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
	EmailLogic emailLogic;

	@Inject
	EventLogic eventLogic;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	RandomLogic randomLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	WbsConfig wbsConfig;

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
	Boolean confirmCharges = false;

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	String rest;

	// state

	State state =
		State.created;

	MessageRec message;
	ChatRec chat;
	ChatUserRec chatUser;
	Integer deliveryId;
	DeliveryRec delivery;
	boolean gotPlace;

	// implementation

	/**
	 * Checks the message for user prefs and updates the user's details with
	 * them.
	 */
	private
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

	private
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

	private
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

			chatUser

				.setOrient (
					chatUser.getGender () == Gender.male
						? Orient.gay
						: Orient.straight);

		}

		if (! male && female && ! both) {

			chatUser

				.setOrient (
					chatUser.getGender () == Gender.male
						? Orient.straight
						: Orient.gay);

		}

		if ((male && female) || both) {

			chatUser

				.setOrient (
					Orient.bi);

		}

	}

	private
	void updateUserDob () {

		Transaction transaction =
			database.currentTransaction ();

		Optional<LocalDate> dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		if (
			isPresent (
				dateOfBirth)
		) {

			chatUser

				.setDob (
					dateOfBirth.get ());

		} else {

			chatUserDobFailureHelper.insert (
				chatUserDobFailureHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setMessage (
					message)

				.setTimestamp (
					transaction.now ())

				.setFailingText (
					textHelper.findOrCreate (
						rest))

			);

			emailLogic.sendSystemEmail (
				ImmutableList.of (
					wbsConfig.developerEmailAddress ()),
				"DOB error",
				stringFormat (

					"********** %s DOB ERROR **********\n",
					wbsConfig.name ().toUpperCase (),
					"\n",

					"Application:  chat\n",
					"Service:      %s.%s\n",
					chat.getSlice ().getCode (),
					chat.getCode (),
					"Timestamp:    %s\n",
					timeFormatter.timestampTimezoneString (
						timeFormatter.timezone (
							ifNull (
								chat.getTimezone (),
								chat.getSlice ().getDefaultTimezone (),
								wbsConfig.defaultTimezone ())),
						transaction.now ()),
					"\n",

					"Message ID:   %s\n",
					message.getId (),
					"Route:        %s.%s\n",
					message.getRoute ().getSlice ().getCode (),
					message.getRoute ().getCode (),
					"Number from:  %s\n",
					message.getNumFrom (),
					"Number to:    %s\n",
					message.getNumTo (),
					"Full message: %s\n",
					message.getText ().getText (),
					"Message rest: %s\n",
					rest));

		}

	}

	private
	boolean updateUserPhoto () {

		ChatUserImageRec chatUserImage =
			chatUserLogic.setPhotoFromMessage (
				chatUser,
				message,
				false);

		if (chatUserImage == null) {

			chatSendLogic.sendSystemMmsFree (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"photo_error",
				commandHelper.findByCodeRequired (
					chatUser.getChatAffiliate (),
					"date_set_photo"),
				TemplateMissing.error);

			return false;

		}

		return true;

	}

	private
	void setAffiliateAndScheme () {

		// set affiliate

		if (chatAffiliateId != null) {

			chatUserLogic.setAffiliate (
				chatUser,
				chatAffiliateHelper.findRequired (
					chatAffiliateId),
				Optional.<MessageRec>fromNullable (
					message));

		}

		// set the chat user's scheme if appropriate

		if (chatSchemeId != null) {

			chatUserLogic.setScheme (
				chatUser,
				chatSchemeHelper.findRequired (
					chatSchemeId));

		}

	}

	/**
	 * Save the appropriate information in this user.
	 */
	private
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

			in (
				joinType,
				JoinType.chatAge)

			&& ChatPatterns.yes.matcher (rest).find ()

		) {

			chatUser

				.setAgeChecked (
					true);

		}

		// confirm charges

		if (

			in (
				joinType,
				JoinType.chatCharges,
				JoinType.dateCharges)

			&& ChatPatterns.yes.matcher (rest).find ()

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
					Optional.of (message),
					Optional.<UserRec>absent ());

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

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				null,
				true);

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"join_error",
				commandHelper.findByCodeRequired (
					chat,
					"help"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check credit

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				Optional.of (
					message.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				null,
				true);

			return false;

		}

		// check age

		if (! chatUserLogic.gotDob (chatUser)) {

			if (chat.getSendDobRequestFromShortcode ()) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"dob_request",
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				chatUser

					.setNextJoinType (
						joinTypeIsChat (joinType)
							? ChatKeywordJoinType.chatDob
							: ChatKeywordJoinType.dateDob);

			} else {

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"dob_request",
					commandHelper.findByCodeRequired (
						chatUser.getChatScheme (),
						joinTypeIsChat (joinType)
							? "chat_dob"
							: "date_dob"),
					0l,
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

			}

			return false;

		}

		if (! chatUserLogic.dobOk (chatUser)) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"dob_too_young",
				commandHelper.findByCodeRequired (
					chat,
					"help"),
				0l,
				TemplateMissing.error,
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
					Optional.of (
						message.getThreadId ()),
					"join_warning",
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"join_warning_2",
					TemplateMissing.ignore,
					Collections.<String,String>emptyMap ());

			} else {

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"join_warning",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"join_warning_2",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.ignore,
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

		if (
			chat.getConfirmCharges ()
			&& ! chatUser.getChargesConfirmed ()
		) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"charges_request",
				commandHelper.findByCodeRequired (
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_charges"
						: "date_charges"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check we got a decent location

		if (in (joinType,
				JoinType.chatLocation,
				JoinType.dateLocation)
			&& ! gotPlace) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"location_error",
				commandHelper.findByCodeRequired (
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_location"
						: "date_location"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check gender

		if (chatUser.getGender () == null) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"gender_request",
				commandHelper.findByCodeRequired (
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_gender"
						: "date_gender"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check orient

		if (chatUser.getOrient () == null) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"gender_other_request",
				commandHelper.findByCodeRequired (
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_gender_other"
						: "date_gender_other"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		// check info

		if (
			chatUser.getInfoText () == null
			&& chatUser.getNewChatUserInfo () == null
		) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				"info_request",
				commandHelper.findByCodeRequired (
					chatUser.getChatScheme (),
					joinTypeIsChat (joinType)
						? "chat_info"
						: "date_info"),
				0l,
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			return false;

		}

		return true;

	}

	private
	boolean checkLocation () {

		// if we have a location that's fine

		if (chatUser.getLocationLongLat () != null)
			return true;

		// otherwise, send an error

		chatSendLogic.sendSystemMagic (
			chatUser,
			Optional.of (
				message.getThreadId ()),
			"location_request",
			commandHelper.findByCodeRequired (
				chatUser.getChatScheme (),
				joinTypeIsChat (joinType)
					? "chat_location"
					: "date_location"),
			0l,
			TemplateMissing.error,
			Collections.<String,String>emptyMap ());

		return false;

	}

	private
	void handleReal () {

		if (! joinPart1 ())
			return;

		joinPart2 ();

	}

	private
	void handleWithState () {

		if (state != State.created) {

			throw new IllegalStateException (
				state.toString ());

		}

		try {

			state = State.inProgress;

			handleReal ();

			state = State.completed;

		} finally {

			if (state == State.inProgress) {
				state = State.error;
			}

		}

	}

	public
	void handleSimple () {

		handleWithState ();

	}

	public
	InboxAttemptRec handleInbox (
			@NonNull CommandRec command) {

		handleWithState ();

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

	private
	boolean joinPart1 () {

		Transaction transaction =
			database.currentTransaction ();

		// lookup stuff

		message =
			inbox != null
				? inbox.getMessage ()
				: null;

		chat =
			chatHelper.findRequired (
				chatId);

		// create chat user

		chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		setAffiliateAndScheme ();

		// make sure the user can join

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				Optional.of (
					message.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
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
					nextRegisterHelpTime);

		}

		// set last action and schedule ad

		chatUser

			.setLastAction (
				transaction.now ());

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
				chatUser.getLocationLongLat ()));

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
				nextQuietOutboundTime);

		// schedule next join outbound message

		Instant nextJoinOutboundTime =
			transaction
				.now ()
				.plus (Duration.standardSeconds (
					+ chat.getTimeJoinOutboundMin ()
					+ randomLogic.randomInteger (
						+ chat.getTimeJoinOutboundMax ()
						- chat.getTimeJoinOutboundMin ())));

		chatUser

			.setNextJoinOutbound (
				nextJoinOutboundTime);

		// bring them into dating if appropriate

		if (joinTypeIsDate (joinType)) {

			if (chatUser.getDateMode () == ChatUserDateMode.none) {

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"date_joined",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.ignore,
					Collections.<String,String>emptyMap ());

			}

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				message,
				chatUser.getMainChatUserImage () != null
					? ChatUserDateMode.text
					: ChatUserDateMode.photo,
				false);

			if (
				chatUser.getDateDailyCount ()
					< chatUser.getDateDailyMax ()
			) {

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

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"more_photos_error",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.error,
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

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"more_videos_error",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.error,
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

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"more_error",
					commandHelper.findByCodeRequired (
						chat,
						"help"),
					0l,
					TemplateMissing.error,
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
			message,
			false);

		// run hooks

		chatHooks.chatUserSignupComplete (
			chatUser);

		return true;

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

	private static
	enum State {

		created,
		inProgress,
		completed,
		error;

	}

}
