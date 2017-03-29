package wbs.apn.chat.user.info.logic;

import static wbs.utils.collection.MapUtils.emptyMap;
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
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserInfo");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			thisUser.getChat ();

		// update chat user with last info stats and charge

		chatCreditLogic.userSpend (
			taskLogger,
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
				taskLogger,
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

		List<String> stringParts;

		try {

			stringParts =
				MessageSplitter.split (
					otherUser.getInfoText ().getText (),
					templates);

		} catch (IllegalArgumentException exception) {

			taskLogger.errorFormatException (
				exception,
				"Error splitting message");

			exceptionLogger.logSimple (
				taskLogger,
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
					taskLogger,
					part));

		}

		chatSendLogic.sendMessageMagic (
			taskLogger,
			thisUser,
			threadIdOptional,
			textParts,
			commandHelper.findByCodeRequired (
				chat,
				"chat"),
			serviceHelper.findByCodeRequired (
				chat,
				serviceCode),
			otherUser.getId (),
			optionalAbsent ());

	}

	@Override
	public
	long sendUserInfos (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserInfos");

		Transaction transaction =
			database.currentTransaction ();

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
				thisUser,
				cutoffTime,
				numToSend);

		for (
			ChatUserRec otherUser
				: otherUsers
		) {

			sendUserInfo (
				taskLogger,
				thisUser,
				otherUser,
				threadIdOptional,
				false);

		}

		return otherUsers.size ();

	}

	@Override
	public
	String chatUserBlurb (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"chatUserBlurb");

		ChatRec chat =
			thisUser.getChat ();

		ChatSchemeRec chatScheme =
			thisUser.getChatScheme ();

		// allocate a magic number

		MagicNumberRec magicNumber =
			magicNumberLogic.allocateMagicNumber (
				taskLogger,
				chatScheme.getMagicNumberSet (),
				thisUser.getNumber (),
				commandHelper.findByCodeRequired (
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

	@Override
	public
	MediaRec chatUserBlurbMedia (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec otherUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"chatUserBlurbMedia");

		String message =
			chatUserBlurb (
				taskLogger,
				thisUser,
				otherUser);

		return mediaLogic.createTextMedia (
			taskLogger,
			message,
			"text/plain",
			otherUser.getCode () + ".txt");

	}

	public
	void sendUserPicsViaLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserPicsViaLink");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			thisUser.getChat ();

		ChatSchemeRec chatScheme =
			thisUser.getChatScheme ();

		// create info site

		ChatInfoSiteRec chatInfoSite =
			chatInfoSiteHelper.insert (
				taskLogger,
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
			taskLogger,
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

		database.flush ();

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
				chat,
				"info_site");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				thisUser);

		messageSender.get ()

			.threadId (
				optionalOrNull (
					threadIdOptional))

			.number (
				thisUser.getNumber ())

			.messageString (
				taskLogger,
				messageText)

			.numFrom (
				chatScheme.getRbNumber ())

			.routerResolve (
				chatScheme.getRbFreeRouter ())

			.service (
				infoSiteService)

			.affiliate (
				affiliate)

			.send (
				taskLogger);

	}

	public
	void sendUserPicsViaMms (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserPicsViaMms");

		Transaction transaction =
			database.currentTransaction ();

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
					taskLogger,
					thisUser,
					otherUser));

			i ++;

		}

		// update chat user with last pic stats and charge

		chatCreditLogic.userSpend (
			taskLogger,
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
				thisUser,
				"system",
				"photos_help_text");

		MediaRec helpTextMedia =
			mediaLogic.createTextMedia (
				taskLogger,
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
				chat,
				serviceCode);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (thisUser);

		messageSender.get ()

			.threadId (
				optionalOrNull (
					threadIdOptional))

			.number (
				thisUser.getNumber ())

			.messageString (
				taskLogger,
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
				taskLogger,
				"User profiles")

			.medias (
				medias)

			.send (
				taskLogger);

	}

	@Override
	public
	void sendUserPics (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserPics");

		Transaction transaction =
			database.currentTransaction ();

		switch (thisUser.getImageMode ()) {

		case link:

			sendUserPicsViaLink (
				taskLogger,
				thisUser,
				otherUsers,
				threadIdOptional,
				asDating);

			break;

		case mms:

			sendUserPicsViaMms (
				taskLogger,
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
					taskLogger,
					otherUser,
					thisUser);

			contact

				.setLastPicTime (
					transaction.now ());

		}

	}

	@Override
	public
	void sendUserVideos (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> otherUsers,
			@NonNull Optional <Long> threadIdOptional,
			@NonNull Boolean asDating) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserVideos");

		Transaction transaction =
			database.currentTransaction ();

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
					taskLogger,
					thisUser,
					otherUser));

			// update contact record with last videostats

			ChatContactRec contact =
				chatContactHelper.findOrCreate (
					taskLogger,
					otherUser,
					thisUser);

			contact

				.setLastVideoTime (
					transaction.now ());

			index ++;

		}

		// update chat user with last video stats and charge

		chatCreditLogic.userSpend (
			taskLogger,
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
				thisUser,
				"system",
				"videos_help_text");

		MediaRec helpTextMedia =
			mediaLogic.createTextMedia (
				taskLogger,
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
				thisUser.getChat (),
				serviceCode);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (thisUser);

		messageSender.get ()

			.threadId (
				optionalOrNull (
					threadIdOptional))

			.number (
				thisUser.getNumber ())

			.messageString (
				taskLogger,
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
				taskLogger,
				"User videos")

			.medias (
				medias)

			.send (
				taskLogger);

	}

	@Override
	public
	long sendRequestedUserPicandOtherUserPics (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec requestedUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendRequestedUserPicandOtherUserPics");

		Transaction transaction =
			database.currentTransaction ();

		Instant cutoffTime =
			transaction.now ()
				.toDateTime ()
				.minusDays (1)
				.toInstant ();

		Collection <ChatUserRec> otherUsers =
			getNearbyOnlineUsersForPic (
				thisUser,
				cutoffTime,
				numToSend);

		ArrayList <ChatUserRec> list =
			new ArrayList<> (
				otherUsers);

		list.add (0, requestedUser);

		otherUsers = list;

		sendUserPics (
			taskLogger,
			thisUser,
			otherUsers,
			threadIdOptional,
			false);

		return otherUsers.size ();

	}

	@Override
	public
	long sendUserPics (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadIdOptional) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserPics");

		Transaction transaction =
			database.currentTransaction ();

		Instant cutoffTime =
			transaction.now ()
				.toDateTime ()
				.minusDays (1)
				.toInstant ();

		Collection<ChatUserRec> otherUsers =
			getNearbyOnlineUsersForPic (
				thisUser,
				cutoffTime,
				numToSend);

		if (otherUsers.size () == 0)
			return 0;

		sendUserPics (
			taskLogger,
			thisUser,
			otherUsers,
			threadIdOptional,
			false);

		return otherUsers.size ();

	}

	@Override
	public
	long sendUserVideos (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec thisUser,
			@NonNull Long numToSend,
			@NonNull Optional <Long> threadId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendUserVideos");

		Transaction transaction =
			database.currentTransaction ();

		Instant cutoffTime =
			transaction.now ()
				.toDateTime ()
				.minusDays (1)
				.toInstant ();

		Collection<ChatUserRec> otherUsers =
			getNearbyOnlineUsersForVideo (
				thisUser,
				cutoffTime,
				numToSend);

		if (otherUsers.size () == 0)
			return 0;

		sendUserVideos (
			taskLogger,
			thisUser,
			otherUsers,
			threadId,
			false);

		return otherUsers.size ();

	}

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForInfo (
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		Collection <ChatUserRec> chatUsers =
			getOnlineUsersForInfo (
				thisUser,
				cutoffTime);

		return chatUserLogic.getNearestUsers (
			thisUser,
			chatUsers,
			numToFind);

	}

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForPic (
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		Collection <ChatUserRec> chatUsers =
			getOnlineUsersForPic (
				thisUser,
				cutoffTime);

		return chatUserLogic.getNearestUsers (
			thisUser,
			chatUsers,
			numToFind);

	}

	@Override
	public
	Collection <ChatUserRec> getNearbyOnlineUsersForVideo (
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime,
			@NonNull Long numToFind) {

		Collection <ChatUserRec> chatUsers =
			getOnlineUsersForVideo (
				thisUser,
				cutoffTime);

		return chatUserLogic.getNearestUsers (
			thisUser,
			chatUsers,
			numToFind);

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
	Collection<ChatUserRec> getOnlineUsersForInfo (
			@NonNull ChatUserRec thisUser,
			@NonNull Instant cutoffTime) {

		ChatRec chat =
			thisUser.getChat ();

		Collection<ChatUserRec> onlineUsers;

		if (
			isNotNull (
				thisUser.getCategory ())
		) {

			onlineUsers =
				chatUserHelper.findOnlineOrMonitorCategory (
					chat,
					thisUser.getCategory ());

		} else {

			onlineUsers =
				chatUserHelper.findOnline (
					chat);

		}

		List<ChatUserRec> ret =
			new ArrayList<ChatUserRec> ();

		for (
			ChatUserRec chatUser
				: onlineUsers
		) {

			// ignore ourselves

			if (chatUser == thisUser)
				continue;

			// ignore blocked users

			if (thisUser.getBlocked ().containsKey (chatUser.getId ()))
				continue;

			// if we aren't suitable gender/orients for each other skip it

			if (
				! chatUserLogic.compatible (
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

	/**
	 * Gets all online users who qualify to send us their pic.
	 */
	private
	Collection<ChatUserRec> getOnlineUsersForPic (
			ChatUserRec thisUser,
			Instant cutoffTime) {

		ChatRec chat =
			thisUser.getChat ();

		Collection<ChatUserRec> onlineChatUsers;

		if (
			isNotNull (
				thisUser.getCategory ())
		) {

			onlineChatUsers =
				chatUserHelper.findOnlineOrMonitorCategory (
					chat,
					thisUser.getCategory ());

		} else {

			onlineChatUsers =
				chatUserHelper.findOnline (
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

			if (! chatUserLogic.compatible (
					thisUser,
					chatUser))
				continue;

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

			selectedChatUsers.add (chatUser);

		}

		return selectedChatUsers;

	}

	private
	Collection<ChatUserRec> getOnlineUsersForVideo (
		ChatUserRec thisUser,
		Instant cutoffTime) {

		ChatRec chat =
			thisUser.getChat ();

		Collection<ChatUserRec> onlineUsers;

		if (
			isNotNull (
				thisUser.getCategory ())
		) {

			onlineUsers =
				chatUserHelper.findOnlineOrMonitorCategory (
					chat,
					thisUser.getCategory ());

		} else {

			onlineUsers =
				chatUserHelper.findOnline (
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

			if (! chatUserLogic.compatible (
					thisUser,
					chatUser))
				continue;

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

	@Override
	public
	void chatUserSetInfo (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser,
			@NonNull String info,
			@NonNull Optional <Long> threadId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"chatUserSetInfo");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		TextRec newInfoText =
			textHelper.findOrCreate (
				taskLogger,
				info);

		// create the chat user info

		ChatUserInfoRec chatUserInfoRec =
			chatUserInfoHelper.insert (
				taskLogger,
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
					taskLogger,
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

	@Override
	public
	void sendNameHint (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendNameHint");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// send the message

		chatSendLogic.sendSystemMagic (
			taskLogger,
			chatUser,
			optionalAbsent (),
			"name_hint",
			commandHelper.findByCodeRequired (
				chat,
				"magic"),
			IdObject.objectId (
				commandHelper.findByCodeRequired (
					chat,
					"name")),
			TemplateMissing.error,
			emptyMap ());

		// and update the chat user

		chatUser

			.setLastNameHint (
				transaction.now ());

	}

	@Override
	public
	void sendPicHint (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendPicHint");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// send the message

		chatSendLogic.sendSystemMmsFree (
			taskLogger,
			chatUser,
			optionalAbsent (),
			"photo_hint",
			commandHelper.findByCodeRequired (
				chat,
				"set_photo"),
			TemplateMissing.error);

		// and update the chat user

		chatUser

			.setLastPicHint (
				transaction.now ());

	}

	@Override
	public
	void sendPicHint2 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendPicHint2");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// send the message

		chatSendLogic.sendSystemMmsFree (
			taskLogger,
			chatUser,
			optionalAbsent (),
			"photo_hint_2",
			commandHelper.findByCodeRequired (
				chat,
				"set_photo"),
			TemplateMissing.error);

		// and update the chat user

		chatUser

			.setLastPicHint (
				transaction.now ());

	}

}
