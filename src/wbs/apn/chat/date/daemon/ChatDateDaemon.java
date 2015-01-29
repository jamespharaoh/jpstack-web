package wbs.apn.chat.date.daemon;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.min;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.LongLat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Log4j
@SingletonComponent ("chatDateDaemon")
public
class ChatDateDaemon
		extends AbstractDaemonService {

	// dependencies

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
	ExceptionLogic exceptionLogic;

	@Inject
	LocatorLogic locatorLogic;

	// properties

	@Getter @Setter
	int datingSleepSeconds = 60 * 60; // one hour

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

				Thread.sleep (datingSleepSeconds * 1000);

			} catch (InterruptedException exception) {

				return;
			}

			// then do the run

			try {

				doRun ();

			} catch (RuntimeException exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"Chat daemon dating",
					exception,
					Optional.<Integer>absent (),
					false);

			}

		}

	}

	void doRun () {

		log.info ("Dating batch started");

		log.debug ("Retrieving list of chats");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<Integer> chatIds =
			new ArrayList<Integer> ();

		for (ChatRec chat
				: chatHelper.findAll ()) {

			chatIds.add (
				chat.getId ());

		}

		transaction.close ();

		for (Integer chatId : chatIds)
			doChat (chatId);

		log.info ("Dating batch complete!");

	}

	void doChat (
			int chatId) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		ChatRec chat =
			chatHelper.find (chatId);

		if (! chat.getDatingEnabled ()) {

			log.warn (
				"Dating disabled for " + chat.getCode ());

			return;

		}

		log.info (
			"Begin dating for " + chat.getCode ());

		int hour =
			transaction
				.now ()
				.toDateTime (chatMiscLogic.timezone (chat))
				.getHourOfDay ();

		LocalDate today =
			LocalDate.now ();

		// first cache some database info to make this a little quicker

		List<DatingUserInfo> otherUserInfos =
			new ArrayList<DatingUserInfo> ();

		List<Integer> datingUserIds =
			new ArrayList<Integer> ();

		int numUsers = 0,
			numCredit = 0,
			numHours = 0,
			numOnline = 0,
			numSent = 0;

		Collection<ChatUserRec> datingUsers =
			chatUserHelper.findDating (
				chat);

		// put all valid dating users into otherUserInfos

		for (
			ChatUserRec chatUser
				: datingUsers
		) {

			if (chatUser.getNumber () == null)
				continue;

			if (chatUser.getNumber ().getNetwork ().getId () == 0)
				continue;

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					chatUser,
					false,
					null);

			if (creditCheckResult.failed ())
				continue;

			otherUserInfos.add (
				new DatingUserInfo (chatUser));

		}

		// then add all the monitors too

		Collection<Integer> monitorIds =
			chatUserHelper.searchIds (
				ImmutableMap.<String,Object>builder ()

			.put (
				"chatId",
				chatId)

			.put (
				"type",
				ChatUserType.monitor)

			.build ());

		for (Integer chatUserId : monitorIds) {

			ChatUserRec chatUser =
				chatUserHelper.find (
					chatUserId);

			otherUserInfos.add (
				new DatingUserInfo (chatUser));

		}

		// put all active dating users who may still need a message into
		// datingUserIds

		for (ChatUserRec chatUser : datingUsers) {

			if (chatUser.getType () != ChatUserType.user) {

				log.debug ("Ignoring " + chatUser.getId () + " (user type)");

				continue;

			}

			numUsers++;

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					chatUser,
					false,
					null);

			if (creditCheckResult.failed ()) {

				numCredit ++;

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

				numHours ++;

				log.debug (
					stringFormat (
						"Ignoring %s ",
						chatUser.getId (),
						"(time)"));

				continue;

			}

			if (chatUser.getOnline ()) {

				numOnline ++;

				log.info ("Ignoring " + chatUser.getId () + " (online)");

				continue;

			}

			if (
				chatUser.getDateDailyDate () != null
				&& equal (chatUser.getDateDailyDate (), today)
				&& chatUser.getDateDailyCount () < 1
			) {

				numSent ++;

				log.debug (
					"Ignoring " + chatUser.getId () + " (daily limit)");

				continue;

			}

			log.debug (
				"Including " + chatUser.getId ());

			datingUserIds.add (
				chatUser.getId ());

		}

		transaction.close ();

		// output some info

		log.info ("Got " + otherUserInfos.size () + " users available to send");

		log.info (String.format (
			"Trying %d out of %d users (%d credit, %d hours, %d online and %d sent already)",
			datingUserIds.size (),
			numUsers,
			numCredit,
			numHours,
			numOnline,
			numSent));

		// then process each user

		int count = 0;
		int max = 1000;

		for (Integer thisUserId : datingUserIds) {
			try {

				if (doUser (otherUserInfos, thisUserId))
					count++;

				if (count >= max)
					break;

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"Chat daemon dating",
					exception,
					Optional.<Integer>absent (),
					false);
			}
		}

		log.info ("Dating done " + count);

	}

	boolean doUser (
			Collection<DatingUserInfo> otherUserInfos,
			int thisUserId) {

		log.info (
			stringFormat (
				"Doing user %s",
				thisUserId));

		boolean status = false;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec thisUser =
			chatUserHelper.find (
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

		int oldDailyCount =
			thisUser.getDateDailyCount ();

		LocalDate oldDailyDate =
			thisUser.getDateDailyDate ();

		if (thisUser.getDateDailyDate () != null) {

			for (LocalDate day = thisUser.getDateDailyDate ();
					day.compareTo (today) < 0;
					day = day.plusDays (1)) {

				int dailyCount =
					thisUser.getDateDailyCount ();

				int dailyMax =
					thisUser.getDateDailyMax ();

				thisUser.setDateDailyCount (
					min (dailyCount + dailyMax, dailyMax));
			}
		}

		thisUser.setDateDailyDate (today);

		log.debug (
			stringFormat (
				"Updated %d: dailyCount %d to %d, dailyDate %s to %s",
				thisUser.getId (),
				oldDailyCount, thisUser.getDateDailyCount (),
				oldDailyDate, thisUser.getDateDailyDate ()));

		// check the user still wants a date message

		if (thisUser.getDateDailyCount () < 1) {
			log.debug ("Ignoring " + thisUserId + " (daily check failed)");
			return false;
		}

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				thisUser,
				false,
				null);

		if (creditCheckResult.failed ()) {

			log.debug (
				stringFormat (
					"Ignoring %s ",
					thisUserId,
					"(%s)",
					creditCheckResult.details ()));

			return false;

		}

		if (! checkHours (hour, thisUser.getDateStartHour (), thisUser.getDateEndHour ())) {
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

		for (DatingUserDistance datingUserDistance
				: prospectiveUserDistances) {

			if (otherUsers.size () == num)
				break;

			ChatUserRec otherUser =
				chatUserHelper.find (
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

		if (equal (thisUser.getId (), thatUserInfo.id))
			return null;

		// check they're not blocked

		if (thisUser.getBlocked ().containsKey (thatUserInfo.id)) {
			dateUserStats.numBlocked++;
			return null;
		}

		// check they are compatible

		if (thisUser.getGender () == null

				|| thisUser.getOrient () == null

				|| ! chatUserLogic.compatible (
					thisUser.getGender (),
					thisUser.getOrient (),
					thatUserInfo.gender,
					thatUserInfo.orient)) {

			dateUserStats.numIncompatible++;

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
							dateToInstant (
								contact.getLastPicTime ()))
					) {

						dateUserStats.numAlreadySent ++;

						return null;

					}

				} else {

					if (

						(
							contact.getLastPicTime () != null
							&& compareTime.isBefore (
								dateToInstant (
									contact.getLastPicTime ()))
						)

						|| (
							contact.getLastInfoTime () != null
							&& compareTime.isBefore (
								dateToInstant (
									contact.getLastInfoTime ()))
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

		if (thisUser.getLocLongLat () == null
				|| thatUserInfo.longLat == null) {

			dateUserStats.numNoLocation++;

			return null;

		}

		int miles = (int)
			locatorLogic.distanceMiles (
				thisUser.getLocLongLat (),
				thatUserInfo.longLat);

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

		int id;
		LongLat longLat;
		boolean photo;
		Gender gender;
		Orient orient;

		DatingUserInfo (
				ChatUserRec chatUser) {

			id = chatUser.getId ();
			longLat = chatUser.getLocLongLat ();
			photo = ! chatUser.getChatUserImageList ().isEmpty ();
			gender = chatUser.getGender ();
			orient = chatUser.getOrient ();

		}

	}

	static
	class DatingUserDistance
		implements Comparable<DatingUserDistance> {

		int id;
		int miles;

		DatingUserDistance (
				int newId,
				int newMiles) {

			id = newId;
			miles = newMiles;

		}

		@Override
		public
		int compareTo (
				DatingUserDistance other) {

			return (Integer.valueOf (miles)).compareTo (
				Integer.valueOf (other.miles));

		}

	}

	public
	boolean checkHours (
			int now,
			int start,
			int end) {

		if (start == end)
			return false;

		if (start < end && (now < start || now >= end))
			return false;

		if (end < start && now < start && now >= end)
			return false;

		return true;

	}

}
