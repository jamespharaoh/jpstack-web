package wbs.apn.chat.date.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.equalSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.min;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.lessThanOne;
import static wbs.utils.etc.NumberUtils.roundToIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.LongLat;

import wbs.utils.time.TimeFormatter;

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
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;

@SingletonComponent ("chatDateDaemon")
public
class ChatDateDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatInfoLogic chatInfoLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	LocatorLogic locatorLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

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

			try (

				OwnedTaskLogger taskLogger =
					logContext.createTaskLogger (
						"runService");

			) {

				try {

					doRun (
						taskLogger);

				} catch (RuntimeException exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						"Chat daemon dating",
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	void doRun (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doRun");

		) {

			taskLogger.noticeFormat (
				"Dating batch started");

			taskLogger.debugFormat (
				"Retrieving list of chats");

			List <Long> chatIds =
				getChatIds (
					taskLogger);

			chatIds.forEach (
				chatId ->
					doChat (
						taskLogger,
						chatId));

			taskLogger.noticeFormat (
				"Dating batch complete!");

		}

	}

	private
	List <Long> getChatIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"getChatIds");

		) {

			return iterableMapToList (
				chatHelper.findNotDeleted (
					transaction),
				ChatRec::getId);

		}

	}

	void doChat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doChat",
					keyEqualsDecimalInteger (
						"chatId",
						chatId));

		) {

			Optional <ChatData> chatDataOptional =
				getChatData (
					taskLogger,
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

			taskLogger.noticeFormat (
				"Got %s otherUserInfos users available to send",
				integerToDecimalString (
					chatData.otherUserInfos.size ()));

			taskLogger.noticeFormat (
				"Trying %s out of %s users ",
				integerToDecimalString (
					chatData.datingUserIds.size ()),
				integerToDecimalString (
					chatData.numUsers),
				"(%s credit, %s hours, %s online and %s sent already)",
				integerToDecimalString (
					chatData.numCredit),
				integerToDecimalString (
					chatData.numHours),
				integerToDecimalString (
					chatData.numOnline),
				integerToDecimalString (
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
							taskLogger,
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
						taskLogger,
						"daemon",
						"Chat daemon dating",
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

			taskLogger.noticeFormat (
				"Dating done %s",
				integerToDecimalString (
					count));

		}

	}

	private
	Optional <ChatData> getChatData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithParameters (
					logContext,
					parentTaskLogger,
					"getChatData",
					keyEqualsDecimalInteger (
						"chatId",
						chatId));

		) {

			ChatData chatData =
				new ChatData ();

			ChatRec chat =
				chatHelper.findRequired (
					transaction,
					chatId);

			if (! chat.getDatingEnabled ()) {

				transaction.warningFormat (
					"Chat dating disabled for %s",
					objectManager.objectPathMini (
						transaction,
						chat));

				return optionalAbsent ();

			}

			transaction.noticeFormat (
				"Start chat dating for %s",
				objectManager.objectPathMini (
					transaction,
					chat));

			long hour =
				transaction.now ()

				.toDateTime (
					chatMiscLogic.timezone (
						transaction,
						chat))

				.getHourOfDay ();

			LocalDate today =
				LocalDate.now ();

			// first cache some database info to make this a little quicker

			transaction.logicFormat (
				"Find dating users");

			Collection <ChatUserRec> datingUsers =
				chatUserHelper.findDating (
					transaction,
					chat);

			// put all valid dating users into otherUserInfos

			transaction.logicFormat (
				"Find valid dating users");

			for (
				ChatUserRec chatUser
					: datingUsers
			) {

				if (chatUser.getNumber () == null) {

					transaction.logicFormat (
						"Chat user %s has no number",
						chatUser.getCode ());

					continue;

				}

				if (chatUser.getNumber ().getNetwork ().getId () == 0) {

					transaction.logicFormat (
						"Chat user %s has no network",
						chatUser.getCode ());

					continue;

				}

				ChatCreditCheckResult creditCheckResult =
					chatCreditLogic.userSpendCreditCheck (
						transaction,
						chatUser,
						false,
						optionalAbsent ());

				if (creditCheckResult.failed ()) {

					transaction.logicFormat (
						"Chat user %s failed credit check: %s",
						creditCheckResult.details ());

					continue;

				}

				transaction.logicFormat (
					"Chat user %s added to dating users");

				chatData.otherUserInfos.add (
					new DatingUserInfo (
						chatUser));

			}

			// then add all the monitors too

			transaction.logicFormat (
				"Find valid dating monitors");

			Collection <Long> monitorIds =
				chatUserHelper.searchIds (
					transaction,
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

				ChatUserRec chatUser =
					chatUserHelper.findRequired (
						transaction,
						chatUserId);

				transaction.logicFormat (
					"Chat monitor %s added to dating users",
					chatUser.getCode ());

				chatData.otherUserInfos.add (
					new DatingUserInfo (
						chatUser));

			}

			transaction.logicFormat (
				"Find dating users to receive a message");

			for (
				ChatUserRec chatUser
					: datingUsers
			) {

				if (chatUser.getType () != ChatUserType.user) {

					transaction.debugFormat (
						"Ignoring %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"(user type)");

					continue;

				}

				chatData.numUsers ++;

				ChatCreditCheckResult creditCheckResult =
					chatCreditLogic.userSpendCreditCheck (
						transaction,
						chatUser,
						false,
						optionalAbsent ());

				if (creditCheckResult.failed ()) {

					chatData.numCredit ++;

					transaction.debugFormat (
						"Ignoring %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"(%s)",
						creditCheckResult.details ());

					continue;

				}

				if (
					! checkHours (
						hour,
						chatUser.getDateStartHour (),
						chatUser.getDateEndHour ())
				) {

					chatData.numHours ++;

					transaction.debugFormat (
						"Ignoring %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"(time)");

					continue;

				}

				if (chatUser.getOnline ()) {

					chatData.numOnline ++;

					transaction.noticeFormat (
						"Ignoring %s (online)",
						integerToDecimalString (
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

					transaction.debugFormat (
						"Ignoring %s (daily limit)",
						objectManager.objectPathMini (
							transaction,
							chatUser));

					continue;

				}

				transaction.logicFormat (
					"Adding user %s to receive a message",
					chatUser.getCode ());

				chatData.datingUserIds.add (
					chatUser.getId ());

			}

			return optionalOf (
				chatData);

		}

	}

	boolean doUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Collection <DatingUserInfo> otherUserInfos,
			@NonNull Long thisUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithParameters (
					logContext,
					parentTaskLogger,
					"doUser",
					keyEqualsDecimalInteger (
						"thisUserId",
						thisUserId));

		) {

			transaction.noticeFormat (
				"Doing user %s",
				integerToDecimalString (
					thisUserId));

			boolean status = false;

			ChatUserRec thisUser =
				chatUserHelper.findRequired (
					transaction,
					thisUserId);

			ChatRec chat =
				thisUser.getChat ();

			int hour =
				transaction
					.now ()
					.toDateTime (
						chatMiscLogic.timezone (
							transaction,
							chat))
					.getHourOfDay ();

			LocalDate today =
				transaction
					.now ()
					.toDateTime (
						chatMiscLogic.timezone (
							transaction,
							chat))
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

			transaction.debugFormat (

				"Updated %s: ",
				integerToDecimalString (
					thisUser.getId ()),

				"dailyCount %s to %s, ",
				integerToDecimalString (
					oldDailyCount),
				integerToDecimalString (
					thisUser.getDateDailyCount ()),

				"dailyDate %s to %s",
				ifNotNullThenElse (
					oldDailyDate,
					() -> timeFormatter.dateString (
						oldDailyDate),
					() -> "none"),
				timeFormatter.dateString (
					thisUser.getDateDailyDate ()));

			// check the user still wants a date message

			if (thisUser.getDateDailyCount () < 1) {

				transaction.debugFormat (
					"Ignoring %s (daily check failed)",
					objectManager.objectPathMini (
						transaction,
						thisUser));

				return false;

			}

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					transaction,
					thisUser,
					false,
					optionalAbsent ());

			if (creditCheckResult.failed ()) {

				transaction.debugFormat (
					"Ignoring %s ",
					integerToDecimalString (
						thisUserId),
					"(%s)",
					creditCheckResult.details ());

				return false;

			}

			if (
				! checkHours (
					hour,
					thisUser.getDateStartHour (),
					thisUser.getDateEndHour ())
			) {

				transaction.debugFormat (
					"Ignoring %s (time check failed)",
					objectManager.objectPathMini (
						transaction,
						thisUser));

				return false;

			}

			if (thisUser.getOnline ()) {

				transaction.debugFormat (
					"Ignoring %s (online check failed)",
					objectManager.objectPathMini (
						transaction,
						thisUser));

				return false;

			}

			if (thisUser.getDateMode () == ChatUserDateMode.none) {

				transaction.debugFormat (
					"Ignoring %s ",
					integerToDecimalString (
						thisUserId),
					"(dating mode check failed)");

				return false;

			}

			// and send them

			if (thisUser.getDateMode () == ChatUserDateMode.photo) {

				if (

					sendSingleLot (
						transaction,
						thisUser,
						otherUserInfos,
						true,
						true,
						false,
						3)

					|| sendSingleLot (
						transaction,
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
						transaction,
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

	}

	boolean sendSingleLot (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <DatingUserInfo> otherUserInfos,
			boolean sendPhoto,
			boolean usersWithPhoto,
			boolean usersWithoutPhoto,
			int num) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendSingleLot");

		) {

			// check each prospective user

			List <DatingUserDistance> prospectiveUserDistances =
				new ArrayList<> ();

			DateUserStats dateUserStats =
				new DateUserStats ();

			for (
				DatingUserInfo thatUserInfo
					: otherUserInfos
			) {

				DatingUserDistance dateUserDistance =
					shouldSendUser (
						transaction,
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

			transaction.noticeFormat (
				"Dating user %s: %s blocked, ",
				integerToDecimalString (
					thisUser.getId ()),
				integerToDecimalString (
					dateUserStats.numBlocked),
				"%s incompatible, ",
				integerToDecimalString (
					dateUserStats.numIncompatible),
				"%s no photo, ",
				integerToDecimalString (
					dateUserStats.numIncompatible),
				"%s already sent, ",
				integerToDecimalString (
					dateUserStats.numIncompatible),
				"%s no location, ",
				integerToDecimalString (
					dateUserStats.numIncompatible),
				"%s too far, ",
				integerToDecimalString (
					dateUserStats.numIncompatible),
				"%s remain",
				integerToDecimalString (
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
						transaction,
						datingUserDistance.id);

				if (otherUser.getInfoText () == null)
					continue;

				otherUsers.add (otherUser);
			}

			if (otherUsers.size () < num) {
				return false; // still too few after the second attempt
			}

			// and send

			if (sendPhoto) {

				chatInfoLogic.sendUserPics (
					transaction,
					thisUser,
					otherUsers,
					null,
					true);

			} else {

				for (
					ChatUserRec otherUser
						: otherUsers
				)

					chatInfoLogic.sendUserInfo (
						transaction,
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
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull DatingUserInfo thatUserInfo,
			boolean sendPhoto,
			boolean usersWithPhoto,
			boolean usersWithoutPhoto,
			boolean reuseOldUsers,
			@NonNull DateUserStats dateUserStats) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"shouldSendUser");

		) {

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
					transaction,
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
