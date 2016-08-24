package wbs.clients.apn.chat.date.daemon;

import static wbs.framework.utils.etc.LogicUtils.allOf;
import static wbs.framework.utils.etc.LogicUtils.equalSafe;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.min;
import static wbs.framework.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.framework.utils.etc.NumberUtils.lessThanOne;
import static wbs.framework.utils.etc.NumberUtils.roundToIntegerRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.clients.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.LongLat;

@Log4j
@SingletonComponent ("chatDateDaemon")
public
class ChatDateDaemon
	extends AbstractDaemonService {

	// dependencies

	@Inject
	ActivityManager activityManager;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	LocatorLogic locatorLogic;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	Duration datingSleepDuration =
		Duration.standardMinutes (
			60);

	// details

	@Override
	protected
	String getThreadName () {

		return "Date";

	}

	@Override
	protected
	void runService () {

		while (true) {

			// sleep first

			try {

				Thread.sleep (
					datingSleepDuration.getMillis ());

			} catch (InterruptedException exception) {

				return;
			}

			// then do the run

			try {

				doRun ();

			} catch (RuntimeException exception) {

				exceptionLogger.logThrowable (
					"daemon",
					"Chat daemon dating",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	void doRun () {

		log.info (
			"Dating batch started");

		log.debug (
			"Retrieving list of chats");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatDateDaemon.doRun ()",
				this);

		List <Long> chatIds =
			chatHelper.findAll ().stream ()

			.map (
				ChatRec::getId)

			.collect (
				Collectors.toList ());

		transaction.close ();

		chatIds.forEach (
			this::doChat);

		log.info ("Dating batch complete!");

	}

	void doChat (
			@NonNull Long chatId) {

		Optional<ChatData> chatDataOptional =
			getChatData (
				chatId);

		if (
			optionalIsNotPresent (
				chatDataOptional)
		) {
			return;
		}

		ChatData chatData =
			optionalGetRequired (
				chatDataOptional);

		// output some info

		log.info (
			stringFormat (
				"Got %s otherUserInfos users available to send",
				chatData.otherUserInfos.size ()));

		log.info (
			stringFormat (
				"Trying %s out of %s users ",
				chatData.datingUserIds.size (),
				chatData.numUsers,
				"(%s credit, %s hours, %s online and %s sent already)",
				chatData.numCredit,
				chatData.numHours,
				chatData.numOnline,
				chatData.numSent));

		// then process each user

		int count = 0;
		int max = 1000;

		for (
			Long thisUserId
				: chatData.datingUserIds
		) {

			try {

				if (
					doUser (
						chatData.otherUserInfos,
						thisUserId)
				) {
					count ++;
				}

				if (count >= max) {
					break;
				}

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"daemon",
					"Chat daemon dating",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

		log.info (
			"Dating done " + count);

	}

	private
	Optional<ChatData> getChatData (
			@NonNull Long chatId) {

		ChatData chatData =
			new ChatData ();

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				stringFormat (
					"%s.%s (%s)",
					getClass ().getSimpleName (),
					"getChatData",
					chatId),
				this);

		ChatRec chat;

		int hour;
		LocalDate today;

		{

			@Cleanup
			ActiveTask activeStep =
				activityManager.start (
					"step",
					"find chat",
					this);

			chat =
				chatHelper.findRequired (
					chatId);

			log.info (
				stringFormat (
					"Start dating for chat %s.%s",
					chat.getSlice ().getCode (),
					chat.getCode ()));

			if (! chat.getDatingEnabled ()) {
	
				log.warn (
					"Dating disabled for " + chat.getCode ());
	
				return Optional.absent ();
	
			}
	
			log.info (
				"Begin dating for " + chat.getCode ());
	
			hour =
				transaction.now ()
	
				.toDateTime (
					chatMiscLogic.timezone (
						chat))
	
				.getHourOfDay ();
	
			today =
				LocalDate.now ();

		}

		Collection<ChatUserRec> datingUsers;

		{

			@Cleanup
			ActiveTask activeStep =
				activityManager.start (
					"step",
					"find dating users",
					this);

			// first cache some database info to make this a little quicker
	
			datingUsers =
				chatUserHelper.findDating (
					chat);

		}

		{

			@Cleanup
			ActiveTask activeStep =
				activityManager.start (
					"step",
					"verify dating users",
					this);

			// put all valid dating users into otherUserInfos

			for (
				ChatUserRec chatUser
					: datingUsers
			) {
	
				@Cleanup
				ActiveTask activeIteration =
					activityManager.start (
						"iteration",
						stringFormat (
							"chat user %s",
							objectManager.objectPathMini (
								chatUser)),
						this);

				if (chatUser.getNumber () == null)
					continue;
	
				if (chatUser.getNumber ().getNetwork ().getId () == 0)
					continue;
	
				ChatCreditCheckResult creditCheckResult =
					chatCreditLogic.userSpendCreditCheck (
						chatUser,
						false,
						Optional.<Long>absent ());
	
				if (creditCheckResult.failed ())
					continue;
	
				chatData.otherUserInfos.add (
					new DatingUserInfo (
						chatUser));
	
			}

		}

		{

			@Cleanup
			ActiveTask activeStep =
				activityManager.start (
					"step",
					"find dating monitors",
					this);

			// then add all the monitors too
	
			Collection<Long> monitorIds =
				chatUserHelper.searchIds (
					new ChatUserSearch ()

				.chatId (
					chatId)

				.type (
					ChatUserType.monitor)

			);
	
			for (
				Long chatUserId
					: monitorIds
			) {
	
				@Cleanup
				ActiveTask activeIteration =
					activityManager.start (
						"iteration",
						stringFormat (
							"chat user %s",
							chatUserId),
						this);

				ChatUserRec chatUser =
					chatUserHelper.findRequired (
						chatUserId);
	
				chatData.otherUserInfos.add (
					new DatingUserInfo (
						chatUser));
	
			}

		}

		{

			@Cleanup
			ActiveTask activeStep =
				activityManager.start (
					"step",
					"select users for dating",
					this);

			// put all active dating users who may still need a message into
			// datingUserIds
	
			for (
				ChatUserRec chatUser
					: datingUsers
			) {

				@Cleanup
				ActiveTask activeIteration =
					activityManager.start (
						"iteration",
						stringFormat (
							"chat user %s",
							objectManager.objectPathMini (
								chatUser)),
						this);

				if (chatUser.getType () != ChatUserType.user) {
	
					log.debug ("Ignoring " + chatUser.getId () + " (user type)");
	
					continue;
	
				}
	
				chatData.numUsers ++;
	
				ChatCreditCheckResult creditCheckResult =
					chatCreditLogic.userSpendCreditCheck (
						chatUser,
						false,
						Optional.<Long>absent ());
	
				if (creditCheckResult.failed ()) {
	
					chatData.numCredit ++;
	
					log.info (
						stringFormat (
							"Ignoring %s ",
							chatUser,
							"(%s)",
							creditCheckResult.details ()));
	
					continue;
	
				}
	
				if (
					! checkHours (
						hour,
						chatUser.getDateStartHour (),
						chatUser.getDateEndHour ())
				) {
	
					chatData.numHours ++;
	
					log.debug (
						stringFormat (
							"Ignoring %s ",
							chatUser.getId (),
							"(time)"));
	
					continue;
	
				}
	
				if (chatUser.getOnline ()) {
	
					chatData.numOnline ++;
	
					log.info (
						stringFormat (
							"Ignoring %s (online)",
							chatUser.getId ()));
	
					continue;
	
				}
	
				if (allOf (

					() -> isNotNull (
						chatUser.getDateDailyDate ()),

					() -> equalSafe (
						chatUser.getDateDailyDate (),
						today),

					() -> lessThanOne (
						chatUser.getDateDailyCount ())

				)) {
	
					chatData.numSent ++;
	
					log.debug (
						"Ignoring " + chatUser.getId () + " (daily limit)");
	
					continue;
	
				}
	
				log.debug (
					"Including " + chatUser.getId ());
	
				chatData.datingUserIds.add (
					chatUser.getId ());
	
			}

		}

		return Optional.of (
			chatData);

	}

	boolean doUser (
			@NonNull Collection<DatingUserInfo> otherUserInfos,
			@NonNull Long thisUserId) {

		log.info (
			stringFormat (
				"Doing user %s",
				thisUserId));

		boolean status = false;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatDateDaemon.doUser (otherUserInfos, thisUserId)",
				this);

		ChatUserRec thisUser =
			chatUserHelper.findRequired (
				thisUserId);

		ChatRec chat =
			thisUser.getChat ();

		int hour =
			transaction
				.now ()
				.toDateTime (chatMiscLogic.timezone (chat))
				.getHourOfDay ();

		LocalDate today =
			transaction
				.now ()
				.toDateTime (chatMiscLogic.timezone (chat))
				.toLocalDate ();

		// inc the daily count thing as appropriate

		long oldDailyCount =
			thisUser.getDateDailyCount ();

		LocalDate oldDailyDate =
			thisUser.getDateDailyDate ();

		if (thisUser.getDateDailyDate () != null) {

			for (
				LocalDate day = thisUser.getDateDailyDate ();
				day.compareTo (today) < 0;
				day = day.plusDays (1)
			) {

				long dailyCount =
					thisUser.getDateDailyCount ();

				long dailyMax =
					thisUser.getDateDailyMax ();

				thisUser.setDateDailyCount (
					min (
						dailyCount + dailyMax,
						dailyMax));

			}

		}

		thisUser

			.setDateDailyDate (
				today);

		log.debug (
			stringFormat (

				"Updated %d: ",
				thisUser.getId (),

				"dailyCount %d to %d, ",
				oldDailyCount,
				thisUser.getDateDailyCount (),

				"dailyDate %s to %s",
				ifNull (oldDailyDate, "none"),
				thisUser.getDateDailyDate ()));

		// check the user still wants a date message

		if (thisUser.getDateDailyCount () < 1) {

			log.debug (
				"Ignoring " + thisUserId + " (daily check failed)");

			return false;

		}

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				thisUser,
				false,
				Optional.<Long>absent ());

		if (creditCheckResult.failed ()) {

			log.debug (
				stringFormat (
					"Ignoring %s ",
					thisUserId,
					"(%s)",
					creditCheckResult.details ()));

			return false;

		}

		if (
			! checkHours (
				hour,
				thisUser.getDateStartHour (),
				thisUser.getDateEndHour ())
		) {

			log.debug ("Ignoring " + thisUserId + " (time check failed)");

			return false;

		}

		if (thisUser.getOnline ()) {

			log.debug ("Ignoring " + thisUserId + " (online check failed)");

			return false;

		}

		if (thisUser.getDateMode () == ChatUserDateMode.none) {

			log.debug ("Ignoring " + thisUserId + " (dating mode check failed)");

			return false;

		}

		// and send them

		if (thisUser.getDateMode () == ChatUserDateMode.photo) {

			if (

				sendSingleLot (
					thisUser,
					otherUserInfos,
					true,
					true,
					false,
					3)

				|| sendSingleLot (
					thisUser,
					otherUserInfos,
					false,
					false,
					true,
					1)

			) {
				status = true;
			}

		} else {

			if (
				sendSingleLot (
					thisUser,
					otherUserInfos,
					false,
					true,
					true,
					1)
			) {
				status = true;
			}

		}

		transaction.commit ();

		return status;

	}

	boolean sendSingleLot (
			ChatUserRec thisUser,
			Collection<DatingUserInfo> otherUserInfos,
			boolean sendPhoto,
			boolean usersWithPhoto,
			boolean usersWithoutPhoto,
			int num) {

		// check each prospective user

		List<DatingUserDistance> prospectiveUserDistances =
			new ArrayList<DatingUserDistance> ();

		DateUserStats dateUserStats =
			new DateUserStats ();

		for (DatingUserInfo thatUserInfo
				: otherUserInfos) {

			DatingUserDistance dateUserDistance =
				shouldSendUser (
					thisUser,
					thatUserInfo,
					sendPhoto,
					usersWithPhoto,
					usersWithoutPhoto,
					true,
					dateUserStats);

			if (dateUserDistance != null)
				prospectiveUserDistances.add (dateUserDistance);
		}

		log.info (
			stringFormat (
				"Dating user %s: %d blocked, %d incompatible, %d no photo, %d" +
				" already sent, %d no location, %d too far, %d remain",
				thisUser.getId (),
				dateUserStats.numBlocked,
				dateUserStats.numIncompatible,
				dateUserStats.numNoPhoto,
				dateUserStats.numAlreadySent,
				dateUserStats.numNoLocation,
				dateUserStats.numTooFar,
				dateUserStats.numOk));

		// now pick the closest few, check they have info

		Collections.sort (
			prospectiveUserDistances);

		List<ChatUserRec> otherUsers =
			new ArrayList<ChatUserRec> ();

		for (
			DatingUserDistance datingUserDistance
				: prospectiveUserDistances
		) {

			if (otherUsers.size () == num)
				break;

			ChatUserRec otherUser =
				chatUserHelper.findRequired (
					datingUserDistance.id);

			if (otherUser.getInfoText () == null)
				continue;

			otherUsers.add (otherUser);
		}

		if (otherUsers.size () < num)
			return false; // still too few after the second attempt

		// and send

		if (sendPhoto) {

			chatInfoLogic.sendUserPics (
				thisUser,
				otherUsers,
				null,
				true);

		} else {

			for (ChatUserRec otherUser : otherUsers)

				chatInfoLogic.sendUserInfo (
					thisUser,
					otherUser,
					null,
					true);
		}

		// and update counter

		thisUser.setDateDailyCount (
			thisUser.getDateDailyCount () - num);

		return true;
	}

	/**
	 * Checks if a given user is eligible to be sent as a dating profile.
	 *
	 * @param thisUser
	 *            The user to receive the profile
	 * @param thatUserInfo
	 *            The user whose profile to check
	 * @return Whether we can send this profile
	 */
	DatingUserDistance shouldSendUser (
			ChatUserRec thisUser,
			DatingUserInfo thatUserInfo,
			boolean sendPhoto,
			boolean usersWithPhoto,
			boolean usersWithoutPhoto,
			boolean reuseOldUsers,
			DateUserStats dateUserStats) {

		Transaction transaction =
			database.currentTransaction ();

		// check its not us

		if (
			integerEqualSafe (
				thisUser.getId (),
				thatUserInfo.id)
		) {
			return null;
		}

		// check they're not blocked

		if (thisUser.getBlocked ().containsKey (thatUserInfo.id)) {
			dateUserStats.numBlocked++;
			return null;
		}

		// check they are compatible

		if (

			isNull (
				thisUser.getGender ())

			|| isNull (
				thisUser.getOrient ())

			|| ! chatUserLogic.compatible (
				thisUser.getGender (),
				thisUser.getOrient (),
				thisUser.getCategory () != null
					? Optional.of (
						thisUser.getCategory ().getId ())
					: Optional.absent (),
				thatUserInfo.gender,
				thatUserInfo.orient,
				thatUserInfo.categoryId)

		) {

			dateUserStats.numIncompatible ++;

			return null;

		}

		// check they have a photo (where appropriate)

		if (sendPhoto && ! thatUserInfo.photo) {
			dateUserStats.numNoPhoto++;
			return null;
		}

		// check we've not already sent it

		ChatContactRec contact =
			thisUser.getFromContacts ().get (thatUserInfo.id);

		if (contact != null) {

			if (reuseOldUsers) {

				Instant compareTime =
					transaction
						.now ()
						.minus (Duration.standardDays (30));

				if (sendPhoto) {

					if (
						contact.getLastPicTime () != null
						&& compareTime.isBefore (
							contact.getLastPicTime ())
					) {

						dateUserStats.numAlreadySent ++;

						return null;

					}

				} else {

					if (

						(
							contact.getLastPicTime () != null
							&& compareTime.isBefore (
								contact.getLastPicTime ())
						)

						|| (
							contact.getLastInfoTime () != null
							&& compareTime.isBefore (
								contact.getLastInfoTime ())
						)

					) {

						dateUserStats.numAlreadySent ++;

						return null;

					}

				}

			} else {

				if (sendPhoto) {

					if (contact.getLastPicTime () != null) {

						dateUserStats.numAlreadySent ++;

						return null;

					}

				} else {

					if (contact.getLastPicTime () != null
							|| contact.getLastInfoTime () != null) {

						dateUserStats.numAlreadySent++;
						return null;
					}
				}
			}
		}

		// check distance

		if (
			thisUser.getLocationLongLat () == null
			|| thatUserInfo.longLat == null
		) {

			dateUserStats.numNoLocation++;

			return null;

		}

		Long miles =
			roundToIntegerRequired (
				locatorLogic.distanceMiles (
					thisUser.getLocationLongLat (),
					thatUserInfo.longLat));

		if (miles > thisUser.getDateRadius ()) {

			dateUserStats.numTooFar ++;

			return null;

		}

		// ok this one is ok

		dateUserStats.numOk ++;

		return new DatingUserDistance (
			thatUserInfo.id,
			miles);

	}

	public static
	class DateUserStats {

		int numBlocked = 0;
		int numIncompatible = 0;
		int numNoPhoto = 0;
		int numAlreadySent = 0;
		int numNoLocation = 0;
		int numTooFar = 0;
		int numOk = 0;

	}

	static
	class DatingUserInfo {

		Long id;
		LongLat longLat;
		boolean photo;

		Gender gender;
		Orient orient;
		Optional<Long> categoryId;

		DatingUserInfo (
				@NonNull ChatUserRec chatUser) {

			id = chatUser.getId ();
			longLat = chatUser.getLocationLongLat ();
			photo = ! chatUser.getChatUserImageList ().isEmpty ();

			gender = chatUser.getGender ();
			orient = chatUser.getOrient ();

			categoryId =
				chatUser.getCategory () != null
					? Optional.of (
						chatUser.getCategory ().getId ())
					: Optional.absent ();

		}

	}

	static
	class DatingUserDistance
		implements Comparable<DatingUserDistance> {

		Long id;
		Long miles;

		DatingUserDistance (
				@NonNull Long newId,
				@NonNull Long newMiles) {

			id =
				newId;

			miles =
				newMiles;

		}

		@Override
		public
		int compareTo (
				@NonNull DatingUserDistance other) {

			return (
				miles.compareTo (
					other.miles));

		}

	}

	public
	boolean checkHours (
			long now,
			long start,
			long end) {

		if (start == end)
			return false;

		if (start < end && (now < start || now >= end))
			return false;

		if (end < start && now < start && now >= end)
			return false;

		return true;

	}

	private static
	class ChatData {

		List<DatingUserInfo> otherUserInfos =
			new ArrayList<DatingUserInfo> ();

		List<Long> datingUserIds =
			new ArrayList<Long> ();

		long numUsers;
		long numCredit;
		long numHours;
		long numOnline;
		long numSent;

	}

}
