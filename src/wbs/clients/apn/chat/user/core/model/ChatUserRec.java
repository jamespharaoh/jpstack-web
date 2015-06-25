package wbs.clients.apn.chat.user.core.model;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.bill.model.ChatUserCreditRec;
import wbs.clients.apn.chat.contact.model.ChatBlockRec;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.image.model.ChatUserImageMode;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.clients.apn.chat.user.info.model.ChatUserNameRec;
import wbs.clients.apn.chat.user.info.model.ChatUserProfileFieldRec;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.locator.model.LongLat;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

@Log4j
@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserRec
	implements CommonRecord<ChatUserRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@CodeField
	String code;

	@SimpleField
	ChatUserType type;

	@ReferenceField (
		nullable = true)
	NumberRec number;

	@ReferenceField (
		nullable = true)
	NumberRec oldNumber;

	// --------------------------------- flags

	@SimpleField
	Boolean online = false;

	@SimpleField
	Boolean ageChecked = false;

	@SimpleField
	Boolean blockAll = false;

	@SimpleField
	Boolean barred = false;

	@SimpleField
	Boolean chargesConfirmed = false;

	@SimpleField
	Boolean joinWarningSent = false;

	@SimpleField
	Boolean broadcastOptOut = false;

	// --------------------------------- prefs and info

	@SimpleField (
		nullable = true)
	Gender gender;

	@SimpleField (
		nullable = true)
	Orient orient;

	@SimpleField (
		nullable = true)
	LocalDate dob;

	@SimpleField (
		nullable = true)
	String name;

	@ReferenceField (
		nullable = true)
	ChatUserNameRec newChatUserName;

	@ReferenceField (
		nullable = true)
	TextRec infoText;

	@ReferenceField (
		nullable = true)
	ChatUserInfoRec newChatUserInfo;

	@SimpleField
	Boolean vetAgain = false;

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	@SimpleField (
		columns = { "location_longitude", "location_latitude" },
		nullable = true)
	LongLat locationLongLat;

	@SimpleField (
		columns = { "location_backup_longitude", "location_backup_latitude" },
		nullable = true)
	LongLat locationBackupLongLat;

	@SimpleField (
		nullable = true)
	Date locationTime;

	@SimpleField (
		nullable = true)
	String locationPlace;

	@SimpleField (
		columns = { "location_place_longitude", "location_place_latitude" },
		nullable = true)
	LongLat locationPlaceLongLat;

	@SimpleField (
		nullable = true)
	Date lastAction;

	@SimpleField (
		nullable = true)
	Date lastJoin;

	@SimpleField (
		nullable = true)
	Date lastSend;

	@SimpleField (
		nullable = true)
	Date lastReceive;

	@SimpleField (
		nullable = true)
	Date lastInfo;

	@SimpleField (
		nullable = true)
	Date lastPic;

	@SimpleField (
		nullable = true)
	Date lastNameHint;

	@SimpleField (
		nullable = true)
	Date lastJoinHint;

	@SimpleField (
		nullable = true)
	Date lastPicHint;

	@SimpleField (
		nullable = true)
	Date lastCreditHint;

	@SimpleField (
		nullable = true)
	Date nextRegisterHelp;

	@SimpleField (
		nullable = true)
	Date nextAd;

	@SimpleField (
		nullable = true)
	Date nextAdultAd;

	@SimpleField (
		nullable = true,
		column = "next_outbound")
	Date nextQuietOutbound;

	@SimpleField (
		nullable = true)
	Date nextJoinOutbound;

	@SimpleField
	Date created = new Date ();

	@SimpleField (
		nullable = true)
	Date firstJoin;

	@ReferenceField (
		nullable = true)
	ChatAffiliateRec chatAffiliate;

	@ReferenceField (
		nullable = true)
	ChatSchemeRec chatScheme;

	@SimpleField (
		nullable = true)
	Date lastBillSent;

	@ReferenceField (
		nullable = true)
	ChatUserImageRec mainChatUserImage;

	@ReferenceField (
		nullable = true)
	ChatUserImageRec mainChatUserVideo;

	@ReferenceField (
		nullable = true)
	ChatUserImageRec mainChatUserAudio;

	@SimpleField (
		nullable = true)
	Date lastMessagePoll;

	@SimpleField
	Integer lastMessagePollId = 0;

	@SimpleField (
		nullable = true)
	ChatMessageMethod deliveryMethod;

	@SimpleField (
		nullable = true)
	Integer lastDeliveryId;

	// --------------------------------- value counters

	@SimpleField
	Integer valueSinceWarning = 0;

	@SimpleField
	Integer valueSinceEver = 0;

	@SimpleField
	Integer credit = 0;

	@SimpleField
	Integer creditPending = 0;

	@SimpleField
	Integer creditPendingStrict = 0;

	@SimpleField
	Integer creditSuccess = 0;

	@SimpleField
	Integer creditFailed = 0;

	@SimpleField
	Integer creditRevoked = 0;

	@SimpleField
	Integer creditRetried = 0;

	@SimpleField
	Integer creditSent = 0;

	@SimpleField
	Integer creditAdded = 0;

	@SimpleField
	Integer creditBought = 0;

	@SimpleField
	ChatUserCreditMode creditMode;

	@SimpleField
	Integer userMessageCount = 0;

	@SimpleField
	Integer userMessageCharge = 0;

	@SimpleField
	Integer monitorMessageCount = 0;

	@SimpleField
	Integer monitorMessageCharge = 0;

	@SimpleField
	Integer textProfileCount = 0;

	@SimpleField
	Integer textProfileCharge = 0;

	@SimpleField
	Integer imageProfileCount = 0;

	@SimpleField
	Integer imageProfileCharge = 0;

	@SimpleField
	Integer videoProfileCount = 0;

	@SimpleField
	Integer videoProfileCharge = 0;

	@SimpleField
	Integer receivedMessageCount = 0;

	@SimpleField
	Integer receivedMessageCharge = 0;

	@SimpleField
	Integer creditDailyAmount = 0;

	@SimpleField (
		nullable = true)
	LocalDate creditDailyDate;

	@SimpleField (
		nullable = true)
	String email;

	@SimpleField (
		nullable = true)
	String jigsawApplicationIdentifier;

	@SimpleField (
		nullable = true)
	String jigsawToken;

	@SimpleField (
		nullable = true)
	Integer sessionInfoRemain;

	// --------------------------------- dating stuff

	@SimpleField
	ChatUserDateMode dateMode =
		ChatUserDateMode.none;

	@SimpleField
	Integer dateRadius = 75;

	@SimpleField
	Integer dateStartHour = 9;

	@SimpleField
	Integer dateEndHour = 21;

	@SimpleField (
		nullable = true)
	LocalDate dateDailyDate;

	@SimpleField
	Integer dateDailyCount = 0;

	@SimpleField
	Integer dateDailyMax = 3;

	@SimpleField (
		nullable = true)
	Date lastDateHint;

	@SimpleField
	Boolean adultVerified = false;

	@SimpleField (
		nullable = true)
	Date adultExpiry;

	@SimpleField (
		nullable = true)
	ChatKeywordJoinType nextJoinType;

	@SimpleField (
		nullable = true)
	Integer monitorCap;

	@SimpleField
	Integer rejectionCount = 0;

	@SimpleField
	Boolean rebillFlag = false;

	@SimpleField
	Integer creditLimit = 1000;

	@SimpleField
	Integer numSpendWarnings = 0;

	@SimpleField
	Integer numImageUploadTokens = 0;

	// --------------------------------- other

	@SimpleField (
		column = "hidden_from_chattube")
	Boolean hiddenFromChatTube = false;

	@SimpleField
	Boolean stealthMonitor = false;

	@SimpleField
	ChatUserImageMode imageMode =
		ChatUserImageMode.link;

	@SimpleField
	ChatUserOperatorLabel operatorLabel =
		ChatUserOperatorLabel.operator;

	// --------------------------------- related objects

	@CollectionField (
		key = "to_user_id",
		index = "from_user_id")
	Map<Integer,ChatContactRec> fromContacts =
		new HashMap<Integer,ChatContactRec> ();

	@CollectionField (
		key = "from_user_id",
		index = "to_user_id")
	Map<Integer,ChatContactRec> toContacts =
		new HashMap<Integer,ChatContactRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatHelpLogRec> chatHelpLogs =
		new TreeSet<ChatHelpLogRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatUserInfoRec> chatUserInfos =
		new TreeSet<ChatUserInfoRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatUserNameRec> chatUserNames =
		new TreeSet<ChatUserNameRec> ();

	@CollectionField (
		index = "blocked_chat_user_id")
	Map<Integer,ChatBlockRec> blocked =
		new HashMap<Integer,ChatBlockRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatUserImageRec> chatUserImages =
		new TreeSet<ChatUserImageRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatUserCreditRec> chatUserCredits =
		new TreeSet<ChatUserCreditRec> ();

	@CollectionField (
		orderBy = "start_time desc")
	Set<ChatUserSessionRec> chatUserSessions =
		new TreeSet<ChatUserSessionRec> ();

	@CollectionField (
		orderBy = "timestamp desc")
	Set<ChatUserDateLogRec> chatUserDateLogs =
		new TreeSet<ChatUserDateLogRec>();

	@CollectionField (
		where = "index IS NOT NULL AND type = 'image'",
		index = "index")
	List<ChatUserImageRec> chatUserImageList =
		new ArrayList<ChatUserImageRec> ();

	@CollectionField (
		where = "index IS NOT NULL AND type = 'video'",
		index = "index")
	List<ChatUserImageRec> chatUserVideoList =
		new ArrayList<ChatUserImageRec> ();

	@CollectionField (
		where = "index IS NOT NULL AND type = 'audio'",
		index = "index")
	List<ChatUserImageRec> chatUserAudioList =
		new ArrayList<ChatUserImageRec> ();

	@CollectionField (
		index = "chat_profile_field_id")
	Map<Integer,ChatUserProfileFieldRec> profileFields =
		new HashMap<Integer,ChatUserProfileFieldRec> ();

	// --------------------------------- utility functions

	public
	ChatUserRec incCreditPending (
			int amount) {

		return setCreditPending (
			getCreditPending () + amount);

	}

	public
	ChatUserRec incCreditPendingStrict (
			int amount) {

		return setCreditPendingStrict (
			getCreditPendingStrict () + amount);

	}

	public
	ChatUserRec incCreditSuccess (
			int amount) {

		return setCreditSuccess (
			getCreditSuccess () + amount);

	}

	public
	ChatUserRec incCreditFailed (
			int amount) {

		return setCreditFailed (
			getCreditFailed () + amount);

	}

	public
	ChatUserRec incCreditRevoked (
			int amount) {

		return setCreditRevoked (
			getCreditRevoked () + amount);

	}

	public
	ChatUserRec incCreditRetried (
			int amount) {

		return setCreditRetried (
			getCreditRetried () + amount);

	}

	public
	ChatUserRec incCreditSent (
			int amount) {

		return setCreditSent (
			getCreditSent () + amount);

	}

	public
	ChatUserRec incCreditBought (
			int amount) {

		return setCreditBought (
			getCreditBought () + amount);

	}

	// TODO move this
	public
	String getPrettyName () {

		if (getName () == null) {
			return getCode ();
		} else {
			return getCode () + " " + getName ();
		}

	}

	@Override
	public
	int compareTo (
			Record<ChatUserRec> otherRecord) {

		ChatUserRec other =
			(ChatUserRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// TODO move this
	public
	List<ChatUserImageRec> getChatUserImageListByType (
			ChatUserImageType type) {

		switch (type) {

			case image:
				return getChatUserImageList ();

			case video:
				return getChatUserVideoList ();

			case audio:
				return getChatUserAudioList ();

			default:
				throw new RuntimeException ();

		}

	}

	// TODO move this
	public
	ChatUserImageRec getMainChatUserImageByType (
			ChatUserImageType type) {

		switch (type) {

			case image:
				return getMainChatUserImage ();

			case video:
				return getMainChatUserVideo ();

			case audio:
				return getMainChatUserAudio ();

			default:
				throw new RuntimeException ();

		}

	}

	// TODO move this
	public
	void setMainChatUserImageByType (
			ChatUserImageType type,
			ChatUserImageRec cui) {

		switch (type) {

			case image:
				setMainChatUserImage (cui);
				break;

			case video:
				setMainChatUserVideo (cui);
				break;

			case audio:
				setMainChatUserAudio (cui);
				break;

			default:
				throw new RuntimeException ();

		}

	}

	// TODO move this
	public
	boolean likes (
			Gender otherGender) {

		if (otherGender == null)
			throw new NullPointerException ("otherGender");

		if (getOrient () == Orient.bi)
			return true;

		if (otherGender == Gender.male) {

			if (
				getGender () == Gender.male
				&& getOrient () == Orient.gay
			) {
				return true;
			}

			if (
				getGender () == Gender.female
				&& getOrient () == Orient.straight
			) {
				return true;
			}

		}

		if (otherGender == Gender.female) {

			if (
				getGender () == Gender.female
				&& getOrient () == Orient.gay
			) {
				return true;
			}

			if (
				getGender () == Gender.male
				&& getOrient () == Orient.straight
			) {
				return true;
			}

		}

		return false;
	}

	// dao methods

	public static
	interface ChatUserDaoMethods {

		ChatUserRec find (
				ChatRec chat,
				NumberRec number);

		int countOnline (
				ChatRec chat,
				ChatUserType type);

		List<ChatUserRec> findOnline (
				ChatRec chat);

		List<ChatUserRec> findOnline (
				ChatUserType type);

		List<ChatUserRec> findWantingBill (
				Date date);

		List<ChatUserRec> findWantingWarning ();

		List<ChatUserRec> findAdultExpiryLimit (
				int maxResults);

		List<ChatUserRec> find (
				ChatRec chat,
				ChatUserType type,
				Orient orient,
				Gender gender);

		List<ChatUserRec> findWantingJoinOutbound ();

		List<ChatUserRec> findWantingAdultAd ();

		List<Integer> searchIds (
				Map<String,Object> searchMap);

		List<ChatUserRec> find (
				ChatAffiliateRec chatAffiliate);

		List<ChatUserRec> findDating (
				ChatRec chat);

		List<ChatUserRec> findWantingAd ();

		List<ChatUserRec> findWantingQuietOutbound ();

	}

	// object helper methods

	public static
	interface ChatUserObjectHelperMethods {

		ChatUserRec findOrCreate (
				ChatRec chat,
				NumberRec number);

		ChatUserRec findOrCreate (
				ChatRec chat,
				MessageRec message);

		ChatUserRec create (
				ChatRec chat,
				NumberRec number);

	}

	// object helper implementation

	public static
	class ChatUserObjectHelperImplementation
		implements ChatUserObjectHelperMethods {

		// dependencies

		@Inject
		RandomLogic randomLogic;

		// indirect dependencies

		@Inject
		Provider<ChatNumberReportLogic> chatNumberReportLogicProvider;

		@Inject
		Provider<ChatUserObjectHelper> chatUserHelperProvider;

		@Inject
		Provider<ChatUserLogic> chatUserLogicProvider;

		@Inject
		Provider<NumberLogic> numberLogicProvider;

		// implementation

		@Override
		public
		ChatUserRec findOrCreate (
				ChatRec chat,
				MessageRec message) {

			// resolve dependencies

			ChatNumberReportLogic chatNumberReportLogic =
				chatNumberReportLogicProvider.get ();

			ChatUserObjectHelper chatUserHelper =
				chatUserHelperProvider.get ();

			NumberLogic numberLogic =
				numberLogicProvider.get ();

			// resolve stuff

			NumberRec number =
				message.getNumber ();

			// check for an existing ChatUser

			ChatUserRec chatUser =
				chatUserHelper.find (
					chat,
					number);

			if (chatUser != null) {

				// check number

				if (
					! chatNumberReportLogic.isNumberReportSuccessful (number)
					&& number.getArchiveDate () == null
				) {

					log.debug (
						stringFormat (
							"Number archiving %s code %s",
							number.getNumber (),
							chatUser.getCode ()));

					NumberRec newNumber =
						numberLogic.archiveNumberFromMessage (
							message);

					return create (
						chat,
						newNumber);

				}

				return chatUser;

			}

			return create (
				chat,
				number);

		}

		@Override
		public
		ChatUserRec findOrCreate (
				ChatRec chat,
				NumberRec number) {

			// resolve dependencies

			ChatUserObjectHelper chatUserHelper =
				chatUserHelperProvider.get ();

			// check for an existing ChatUser

			ChatUserRec chatUser =
				chatUserHelper.find (
					chat,
					number);

			if (chatUser != null)
				return chatUser;

			return create (
				chat,
				number);

		}

		@Override
		public
		ChatUserRec create (
				ChatRec chat,
				NumberRec number) {

			ChatUserObjectHelper chatUserHelper =
				chatUserHelperProvider.get ();

			ChatUserLogic chatUserLogic =
				chatUserLogicProvider.get ();

			// create him

			ChatUserRec chatUser =
				new ChatUserRec ()

				.setChat (
					chat)

				.setNumber (
					number)

				.setOldNumber (
					number)

				.setType (
					ChatUserType.user)

				.setCode (
					randomLogic.generateNumericNoZero (6))

				.setDeliveryMethod (
					ChatMessageMethod.sms)

				.setGender (
					chat.getGender ())

				.setOrient (
					chat.getOrient ())

				.setCreditMode (
					number.getFree ()
						? ChatUserCreditMode.free
						: ChatUserCreditMode.strict);

			chatUserLogic.monitorCap (
				chatUser);

			// set adult verify on some services
			// TODO this should probably not be here

			if (chat.getAutoAdultVerify ()) {

				chatUserLogic.adultVerify (
					chatUser);

			}

			chatUserHelper.insert (
				chatUser);

			return chatUser;

		}

	}

	// object hooks

	public static
	class ChatUserHooks
		extends AbstractObjectHooks<ChatUserRec> {

		// dependencies

		@Inject
		ChatUserDao chatUserDao;

		// implementation

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			@SuppressWarnings ("unchecked")
			Map<String,Object> chatUserSearch =
				(Map<String,Object>) search;

			return chatUserDao.searchIds (
				chatUserSearch);

		}

		@Override
		public
		void beforeUpdate (
				ChatUserRec object) {

			System.out.println (
				"BEFORE UPDATE " + object);

		}

	}

}
