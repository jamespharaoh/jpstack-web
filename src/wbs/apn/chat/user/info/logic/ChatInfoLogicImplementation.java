package wbs.apn.chat.user.info.logic;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.roundToIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.info.model.ChatUserInfoObjectHelper;
import wbs.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;

@SingletonComponent ("chatInfoLogic")
public
class ChatInfoLogicImplementation
	implements ChatInfoLogic {

	// singleton dependencies

	@SingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatHelpTemplateLogic chatTemplateLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserInfoObjectHelper chatUserInfoHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@SingletonDependency
	LocatorLogic locatorLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberLogic magicNumberLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// implementation

	@Override
	public
	void sendUserInfo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserInfo");

		) {

			ChatRec chat =
				thisUser.getChat ();

			// update chat user with last info stats and charge

			chatCreditLogic.userSpend (
				transaction,
				thisUser,
				0,
				0,
				1,
				0,
				0);

			thisUser

				.setLastInfo (
					transaction.now ());

			// update contact record with last info stats

			ChatContactRec contact =
				chatContactHelper.findOrCreate (
					transaction,
					otherUser,
					thisUser);

			contact

				.setLastInfoTime (
					transaction.now ());

			// work out distance

			long miles =
				roundToIntegerRequired (
					locatorLogic.distanceMiles (
						thisUser.getLocationLongLat (),
						otherUser.getLocationLongLat ()));

			// construct message parts

			String userId =
				otherUser.getName () == null
					? otherUser.getCode ()
					: otherUser.getName () + " " + otherUser.getCode ();

			String distanceText =
				 "" + miles + (miles == 1 ? " mile" : " miles");

			MessageSplitter.Templates templates =
				chatTemplateLogic.splitter (
					transaction,
					chat,
					otherUser.getType () == ChatUserType.monitor
						? "info_monitor"
						: "info_user",
					ImmutableMap.<String,String>builder ()

						.put (
							"user",
							userId)

						.put (
							"distance",
							distanceText)

						.build ());

			List <String> stringParts;

			try {

				stringParts =
					MessageSplitter.split (
						otherUser.getInfoText ().getText (),
						templates);

			} catch (IllegalArgumentException exception) {

				transaction.errorFormatException (
					exception,
					"Error splitting message");

				exceptionLogger.logSimple (
					transaction,
					"unknown",
					"chatLogic.sendUserInfo (...)",
					"MessageSplitter.split (...) threw IllegalArgumentException",

					stringFormat (
						"Error probably caused by illegal characters in user's ",
						"info. Ignoring error.\n",
						"\n",
						"thisUser.id = " + thisUser.getId () + "\n",
						"otherUser.id = " + otherUser.getId () + "\n",
						"\n",
						"%s",
						exceptionLogic.throwableDump (
							transaction,
							exception)),

					Optional.absent (),
					GenericExceptionResolution.ignoreWithNoWarning);

				return;

			}

			// send the message

			String serviceCode =
				asDating
					? "date_text"
					: "online_text";

			List<TextRec> textParts =
				new ArrayList<TextRec> ();

			for (
				String part
					: stringParts
			) {

				textParts.add (
					textHelper.findOrCreate (
						transaction,
						part));

			}

			chatSendLogic.sendMessageMagic (
				transaction,
				thisUser,
				threadIdOptional,
				textParts,
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"chat"),
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					serviceCode),
				otherUser.getId (),
				optionalAbsent ());

		}

	}

	@Override
	public
	long sendUserInfos (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserInfos");

		) {

			// ignore deleted users

			if (thisUser.getNumber () == null)
				return 0;

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusWeeks (1)
					.toInstant ();

			Collection <ChatUserRec> otherUsers =
				getNearbyOnlineUsersForInfo (
					transaction,
					thisUser,
					cutoffTime,
					numToSend);

			for (
				ChatUserRec otherUser
					: otherUsers
			) {

				sendUserInfo (
					transaction,
					thisUser,
					otherUser,
					threadIdOptional,
					false);

			}

			return otherUsers.size ();

		}

	}

	@Override
	public
	String chatUserBlurb (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatUserBlurb");

		) {

			ChatRec chat =
				thisUser.getChat ();

			ChatSchemeRec chatScheme =
				thisUser.getChatScheme ();

			// allocate a magic number

			MagicNumberRec magicNumber =
				magicNumberLogic.allocateMagicNumber (
					transaction,
					chatScheme.getMagicNumberSet (),
					thisUser.getNumber (),
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"chat"),
					otherUser.getId ());

			StringBuilder message =
				new StringBuilder ();

			message.append (
				stringFormat (
					"Text me on: 0%s\n",
					magicNumber.getNumber ().substring (2)));

			if (otherUser.getName () != null)

				message.append (
					stringFormat (
						"Name: %s\n",
						otherUser.getName ()));

			message.append (
				stringFormat (
					"User: %s\n",
					otherUser.getCode ()));

			if (otherUser.getInfoText () != null) {

				message.append (
					stringFormat (
						"Info: %s\n",
						otherUser.getInfoText ().getText ()));

			}

			return message.toString ();

		}

	}

	@Override
	public
	MediaRec chatUserBlurbMedia (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatUserBlurbMedia");

		) {

			String message =
				chatUserBlurb (
					transaction,
					thisUser,
					otherUser);

			return mediaLogic.createTextMedia (
				transaction,
				message,
				"text/plain",
				otherUser.getCode () + ".txt");

		}

	}

	public
	void sendUserPicsViaLink (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserPicsViaLink");

		) {

			ChatRec chat =
				thisUser.getChat ();

			ChatSchemeRec chatScheme =
				thisUser.getChatScheme ();

			// create info site

			ChatInfoSiteRec chatInfoSite =
				chatInfoSiteHelper.insert (
					transaction,
					chatInfoSiteHelper.createInstance ()

				.setChatUser (
					thisUser)

				.setCreateTime (
					transaction.now ())

				.setNumViews (
					0l)

				.setNumExpired (
					0l)

				.setExpireTime (
					Instant.now ().plus (
						Duration.standardHours (48)))

				.setToken (
					randomLogic.generateLowercase (10))

				.setOtherChatUsers (
					ImmutableList.copyOf (
						otherUsers))

			);

			// update user charge and stats

			chatCreditLogic.userSpend (
				transaction,
				thisUser,
				0,
				0,
				0,
				otherUsers.size (),
				0);

			thisUser

				.setLastPic (
					transaction.now ());

			// flush to generate id

			transaction.flush ();

			// send the wap link

			/*
			wapPushUtils.wapPushSend (
				null,
				thisUser.getNumber (),
				chatScheme.getRbNumber (),
				"Click for user pics",
				sf ("http://hades.apnuk.com/api/chat/infoSite/%s/%s",
					infoSite.getId (),
					infoSite.getToken ()),
				chatScheme.getWapRouter ().getRoute (),
				serviceLogic.findOrCreateService (
					chat,
					"info_site",
					"info_site"),
				null,
				getAffiliate (thisUser),
				null,
				null,
				true,
				null,
				null,
				null);
			*/

			ChatHelpTemplateRec linkTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					"info_site_link");

			String link =
				stringFormat (
					"http://txtit.com/is/%s/%s",
					integerToDecimalString (
						chatInfoSite.getId ()),
					chatInfoSite.getToken ());

			String messageText =
				linkTemplate.getText ()
					.replace ("{link}", link);

			ServiceRec infoSiteService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"info_site");

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					thisUser);

			messageSender.get ()

				.threadId (
					optionalOrNull (
						threadIdOptional))

				.number (
					thisUser.getNumber ())

				.messageString (
					transaction,
					messageText)

				.numFrom (
					chatScheme.getRbNumber ())

				.routerResolve (
					transaction,
					chatScheme.getRbFreeRouter ())

				.service (
					infoSiteService)

				.affiliate (
					affiliate)

				.send (
					transaction);

		}

	}

	public
	void sendUserPicsViaMms (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserPicsViaMms");

		) {

			ChatRec chat =
				thisUser.getChat ();

			ChatSchemeRec chatScheme =
				thisUser.getChatScheme ();

			List<MediaRec> medias =
				new ArrayList<MediaRec> ();

			int i = 0;

			for (
				ChatUserRec otherUser
					: otherUsers
			) {

				// add their image to the list

				if (otherUser.getMainChatUserImage () != null) {

					medias.add (
						otherUser.getMainChatUserImage ().getMedia ());

				}

				// add their blurb

				medias.add (
					chatUserBlurbMedia (
						transaction,
						thisUser,
						otherUser));

				i ++;

			}

			// update chat user with last pic stats and charge

			chatCreditLogic.userSpend (
				transaction,
				thisUser,
				0,
				0,
				0,
				i,
				0);

			thisUser

				.setLastPic (
					transaction.now ());

			// now add a help message on the end

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					thisUser,
					"system",
					"photos_help_text");

			MediaRec helpTextMedia =
				mediaLogic.createTextMedia (
					transaction,
					chatHelpTemplate.getText (),
					"text/plain",
					"help.text");

			medias.add (helpTextMedia);

			// and send the message

			String serviceCode =
				asDating
					? "date_image"
					: "online_image";

			ServiceRec service =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					serviceCode);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					thisUser);

			messageSender.get ()

				.threadId (
					optionalOrNull (
						threadIdOptional))

				.number (
					thisUser.getNumber ())

				.messageString (
					transaction,
					"")

				.numFrom (
					chatScheme.getMmsNumber ())

				.route (
					chatScheme.getMmsRoute ())

				.service (
					service)

				.affiliate (
					affiliate)

				.subjectString (
					transaction,
					"User profiles")

				.medias (
					medias)

				.send (
					transaction);

		}

	}

	@Override
	public
	void sendUserPics (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserPics");

		) {

			switch (thisUser.getImageMode ()) {

			case link:

				sendUserPicsViaLink (
					transaction,
					thisUser,
					otherUsers,
					threadIdOptional,
					asDating);

				break;

			case mms:

				sendUserPicsViaMms (
					transaction,
					thisUser,
					otherUsers,
					threadIdOptional,
					asDating);

				break;

			default:

				throw new RuntimeException ("Error 10842342");

			}

			// update contact record with last pic stats

			for (
				ChatUserRec otherUser
					: otherUsers
			) {

				ChatContactRec contact =
					chatContactHelper.findOrCreate (
						transaction,
						otherUser,
						thisUser);

				contact

					.setLastPicTime (
						transaction.now ());

			}

		}

	}

	@Override
	public
	void sendUserVideos (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserVideos");

		) {

			ChatSchemeRec chatScheme =
				thisUser.getChatScheme ();

			List <MediaRec> medias =
				new ArrayList<> ();

			int index = 0;

			for (
				ChatUserRec otherUser
					: otherUsers
			) {

				// add their video to the list

				if (otherUser.getMainChatUserVideo () != null) {

					medias.add (
						otherUser.getMainChatUserVideo ().getMedia ());

				}

				// add their blurb

				medias.add (
					chatUserBlurbMedia (
						transaction,
						thisUser,
						otherUser));

				// update contact record with last videostats

				ChatContactRec contact =
					chatContactHelper.findOrCreate (
						transaction,
						otherUser,
						thisUser);

				contact

					.setLastVideoTime (
						transaction.now ());

				index ++;

			}

			// update chat user with last video stats and charge

			chatCreditLogic.userSpend (
				transaction,
				thisUser,
				0,
				0,
				0,
				0,
				index);

			thisUser

				.setLastPic (
					transaction.now ());

			// now add a help message on the end

			ChatHelpTemplateRec template =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					thisUser,
					"system",
					"videos_help_text");

			MediaRec helpTextMedia =
				mediaLogic.createTextMedia (
					transaction,
					template.getText (),
					"text/plain",
					"help.text");

			medias.add (
				helpTextMedia);

			// and send the message

			String serviceCode =
				asDating
					? "date_video"
					: "online_video";

			ServiceRec service =
				serviceHelper.findByCodeRequired (
					transaction,
					thisUser.getChat (),
					serviceCode);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					thisUser);

			messageSender.get ()

				.threadId (
					optionalOrNull (
						threadIdOptional))

				.number (
					thisUser.getNumber ())

				.messageString (
					transaction,
					"")

				.numFrom (
					chatScheme.getMmsNumber ())

				.route (
					chatScheme.getMmsRoute ())

				.service (
					service)

				.affiliate (
					affiliate)

				.subjectString (
					transaction,
					"User videos")

				.medias (
					medias)

				.send (
					transaction);

		}

	}

	@Override
	public
	long sendRequestedUserPicandOtherUserPics (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec requestedUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendRequestedUserPicandOtherUserPics");

		) {

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusDays (1)
					.toInstant ();

			Collection <ChatUserRec> otherUsers =
				getNearbyOnlineUsersForPic (
					transaction,
					thisUser,
					cutoffTime,
					numToSend);

			ArrayList <ChatUserRec> list =
				new ArrayList<> (
					otherUsers);

			list.add (0, requestedUser);

			otherUsers = list;

			sendUserPics (
				transaction,
				thisUser,
				otherUsers,
				threadIdOptional,
				false);

			return otherUsers.size ();

		}

	}

	@Override
	public
	long sendUserPics (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserPics");

		) {

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusDays (1)
					.toInstant ();

			Collection <ChatUserRec> otherUsers =
				getNearbyOnlineUsersForPic (
					transaction,
					thisUser,
					cutoffTime,
					numToSend);

			if (otherUsers.size () == 0)
				return 0;

			sendUserPics (
				transaction,
				thisUser,
				otherUsers,
				threadIdOptional,
				false);

			return otherUsers.size ();

		}

	}

	@Override
	public
	long sendUserVideos (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendUserVideos");

		) {

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusDays (1)
					.toInstant ();

			Collection <ChatUserRec> otherUsers =
				getNearbyOnlineUsersForVideo (
					transaction,
					thisUser,
					cutoffTime,
					numToSend);

			if (otherUsers.size () == 0)
				return 0;

			sendUserVideos (
				transaction,
				thisUser,
				otherUsers,
				threadId,
				false);

			return otherUsers.size ();

		}

	}

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForInfo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getNearbyOnlineUsersForInfo");

		) {

			Collection <ChatUserRec> chatUsers =
				getOnlineUsersForInfo (
					transaction,
					thisUser,
					cutoffTime);

			return chatUserLogic.getNearestUsers (
				transaction,
				thisUser,
				chatUsers,
				numToFind);

		}

	}

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForPic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getNearbyOnlineUsersForPic");

		) {

			Collection <ChatUserRec> chatUsers =
				getOnlineUsersForPic (
					transaction,
					thisUser,
					cutoffTime);

			return chatUserLogic.getNearestUsers (
				transaction,
				thisUser,
				chatUsers,
				numToFind);

		}

	}

	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForVideo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getNearbyOnlineUsersForVideo");

		) {

			Collection <ChatUserRec> chatUsers =
				getOnlineUsersForVideo (
					transaction,
					thisUser,
					cutoffTime);

			return chatUserLogic.getNearestUsers (
				transaction,
				thisUser,
				chatUsers,
				numToFind);

		}

	}

	/**
	 * Gets all online users who qualify to send us their pic. Also takes a
	 * cutoff time, anyone who we have been sent the picture of since this time
	 * won't be included.
	 *
	 * @param thisUser
	 *            the user who wants to receive pics
	 * @param cutoffTime
	 *            the cutoff time
	 * @return a collection of eligible chat users
	 */
	private
	Collection <ChatUserRec> getOnlineUsersForInfo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getOnlineUsersForInfo");

		) {

			ChatRec chat =
				thisUser.getChat ();

			Collection <ChatUserRec> onlineUsers;

			if (
				isNotNull (
					thisUser.getCategory ())
			) {

				onlineUsers =
					chatUserHelper.findOnlineOrMonitorCategory (
						transaction,
						chat,
						thisUser.getCategory ());

			} else {

				onlineUsers =
					chatUserHelper.findOnline (
						transaction,
						chat);

			}

			List <ChatUserRec> ret =
				new ArrayList<> ();

			for (
				ChatUserRec chatUser
					: onlineUsers
			) {

				// ignore ourselves

				if (chatUser == thisUser)
					continue;

				// ignore blocked users

				if (
					mapContainsKey (
						thisUser.getBlocked (),
						chatUser.getId ())
				) {
					continue;
				}

				// if we aren't suitable gender/orients for each other skip it

				if (
					! chatUserLogic.compatible (
						transaction,
						thisUser,
						chatUser)
				) {

					continue;

				}

				// ignore users we have had an info, message or pic from recently

				ChatContactRec chatUserContact =
					thisUser.getFromContacts ().get (
						chatUser.getId ());

				if (chatUserContact != null) {

					if (

						isNotNull (
							chatUserContact.getLastInfoTime ())

						&& laterThan (
							chatUserContact.getLastInfoTime (),
							cutoffTime)

					) {
						continue;
					}

					if (

						isNotNull (
							chatUserContact.getLastDeliveredMessageTime ())

						&& laterThan (
							chatUserContact.getLastDeliveredMessageTime (),
							cutoffTime)

					) {
						continue;
					}

					if (

						isNotNull (
							chatUserContact.getLastPicTime ())

						&& laterThan (
							chatUserContact.getLastPicTime (),
							cutoffTime)

					) {
						continue;
					}

				}

				// ignore users with no info

				if (chatUser.getInfoText () == null)
					continue;

				// ignore system user

				if (chatUser == chat.getSystemChatUser ())
					continue;

				ret.add (chatUser);

			}

			return ret;

		}

	}

	/**
	 * Gets all online users who qualify to send us their pic.
	 */
	private
	Collection <ChatUserRec> getOnlineUsersForPic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getOnlineUsersForPic");

		) {

			ChatRec chat =
				thisUser.getChat ();

			Collection <ChatUserRec> onlineChatUsers;

			if (
				isNotNull (
					thisUser.getCategory ())
			) {

				onlineChatUsers =
					chatUserHelper.findOnlineOrMonitorCategory (
						transaction,
						chat,
						thisUser.getCategory ());

			} else {

				onlineChatUsers =
					chatUserHelper.findOnline (
						transaction,
						chat);

			}

			List<ChatUserRec> selectedChatUsers =
				new ArrayList<ChatUserRec> ();

			for (
				ChatUserRec chatUser
					: onlineChatUsers
			) {

				// ignore ourselves

				if (chatUser == thisUser)
					continue;

				// ignore blocked users

				if (thisUser.getBlocked ().containsKey (
						chatUser.getId ()))
					continue;

				// if we aren't suitable gender/orients for each other skip it

				if (
					! chatUserLogic.compatible (
						transaction,
						thisUser,
						chatUser)
				) {
					continue;
				}

				// ignore users we have had a pic from recently

				ChatContactRec chatContact =
					thisUser.getFromContacts ().get (
						chatUser.getId ());

				if (

					isNotNull (
						chatContact)

					&& isNotNull (
						chatContact.getLastPicTime ())

					&& laterThan (
						chatContact.getLastPicTime (),
						cutoffTime)

				) {
					continue;
				}

				// ignore users with no info or main pic

				if (chatUser.getInfoText () == null)
					continue;

				if (chatUser.getMainChatUserImage () == null)
					continue;

				// ignore users according to monitor cap

				if (thisUser.getMonitorCap () != null
						&& chatUser.getType () == ChatUserType.monitor
						&& (chatUser.getCode ().charAt (2) - '0') < thisUser
								.getMonitorCap ())
					continue;

				// ignore system user

				if (chatUser == chat.getSystemChatUser ())
					continue;

				selectedChatUsers.add (
					chatUser);

			}

			return selectedChatUsers;

		}

	}

	private
	Collection <ChatUserRec> getOnlineUsersForVideo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getOnlineUsersForVideo");

		) {

			ChatRec chat =
				thisUser.getChat ();

			Collection<ChatUserRec> onlineUsers;

			if (
				isNotNull (
					thisUser.getCategory ())
			) {

				onlineUsers =
					chatUserHelper.findOnlineOrMonitorCategory (
						transaction,
						chat,
						thisUser.getCategory ());

			} else {

				onlineUsers =
					chatUserHelper.findOnline (
						transaction,
						chat);

			}

			List<ChatUserRec> selectedChatUsers =
				new ArrayList<ChatUserRec> ();

			for (
				ChatUserRec chatUser
					: onlineUsers
			) {

				// ignore ourselves

				if (chatUser == thisUser)
					continue;

				// ignore blocked users

				if (
					thisUser.getBlocked ().containsKey (
						chatUser.getId ())
				) {
					continue;
				}

				// if we aren't suitable gender/orients for each other skip it

				if (
					! chatUserLogic.compatible (
						transaction,
						thisUser,
						chatUser)
				) {
					continue;
				}

				// ignore users we have had a videofrom recently

				ChatContactRec chatContact =
					thisUser.getFromContacts ().get (
						chatUser.getId ());

				if (

					isNotNull (
						chatContact)

					&& isNotNull (
						chatContact.getLastVideoTime ())

					&& laterThan (
						chatContact.getLastVideoTime (),
						cutoffTime)

				) {
					continue;
				}

				// ignore users with no info or main video

				if (chatUser.getInfoText () == null)
					continue;

				if (chatUser.getMainChatUserVideo () == null)
					continue;

				// ignore users according to monitor cap

				if (thisUser.getMonitorCap () != null
						&& chatUser.getType () == ChatUserType.monitor
						&& (chatUser.getCode ().charAt (2) - '0')
							< thisUser.getMonitorCap ())
					continue;

				// ignore system user

				if (chatUser == chat.getSystemChatUser ())
					continue;

				selectedChatUsers.add (
					chatUser);

			}

			return selectedChatUsers;

		}

	}

	@Override
	public
	void chatUserSetInfo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull String info,
			@NonNull Optional <Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatUserSetInfo");

		) {

			ChatRec chat =
				chatUser.getChat ();

			TextRec newInfoText =
				textHelper.findOrCreate (
					transaction,
					info);

			// create the chat user info

			ChatUserInfoRec chatUserInfoRec =
				chatUserInfoHelper.insert (
					transaction,
					chatUserInfoHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setCreationTime (
					transaction.now ())

				.setOriginalText (
					newInfoText)

				.setStatus (
					ChatUserInfoStatus.moderatorPending)

				.setThreadId (
					optionalOrNull (
						threadId))

			);

			chatUser

				.setNewChatUserInfo (
					chatUserInfoRec);

			// create a queue item if necessary

			if (chatUser.getQueueItem () == null) {

				QueueItemRec queueItem =
					queueLogic.createQueueItem (
						transaction,
						chat,
						"user",
						chatUser,
						chatUser,
						chatUserLogic.getPrettyName (
							chatUser),
						"Info to approve");

				chatUser

					.setQueueItem (
						queueItem);

			}

		}

	}

	@Override
	public
	void sendNameHint (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendNameHint");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// send the message

			chatSendLogic.sendSystemMagic (
				transaction,
				chatUser,
				optionalAbsent (),
				"name_hint",
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"magic"),
				IdObject.objectId (
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"name")),
				TemplateMissing.error,
				emptyMap ());

			// and update the chat user

			chatUser

				.setLastNameHint (
					transaction.now ());

		}

	}

	@Override
	public
	void sendPicHint (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendPicHint");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// send the message

			chatSendLogic.sendSystemMmsFree (
				transaction,
				chatUser,
				optionalAbsent (),
				"photo_hint",
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"set_photo"),
				TemplateMissing.error);

			// and update the chat user

			chatUser

				.setLastPicHint (
					transaction.now ());

		}

	}

	@Override
	public
	void sendPicHint2 (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendPicHint2");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// send the message

			chatSendLogic.sendSystemMmsFree (
				transaction,
				chatUser,
				optionalAbsent (),
				"photo_hint_2",
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"set_photo"),
				TemplateMissing.error);

			// and update the chat user

			chatUser

				.setLastPicHint (
					transaction.now ());

		}

	}

}
