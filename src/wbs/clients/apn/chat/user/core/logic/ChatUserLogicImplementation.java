package wbs.clients.apn.chat.user.core.logic;

import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualSafe;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.framework.utils.etc.NumberUtils.notLessThan;
import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalFromJava;
import static wbs.framework.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringInSafe;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;
import static wbs.framework.utils.etc.TimeUtils.calculateAgeInYears;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;
import static wbs.framework.utils.etc.TimeUtils.localDate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeMapRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.clients.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.EmailLogic;
import wbs.framework.utils.RandomLogic;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeObjectHelper;
import wbs.platform.media.model.MediaTypeRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;

@SingletonComponent ("chatUserLogic")
public
class ChatUserLogicImplementation
	implements ChatUserLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ChatNumberReportLogic chatNumberReportLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserImageObjectHelper chatUserImageHelper;

	@Inject
	Database database;

	@Inject
	EmailLogic emailLogic;

	@Inject
	EventLogic eventLogic;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@Inject
	KeywordFinder keywordFinder;

	@Inject
	LocatorLogic locatorLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MediaTypeObjectHelper mediaTypeHelper;

	@Inject
	NumberLogic numberLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Inject
	RandomLogic randomLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	AffiliateRec getAffiliate (
			@NonNull ChatUserRec chatUser) {

		if (chatUser.getChatAffiliate () != null) {

			return affiliateHelper.findByCodeRequired (
				chatUser.getChatAffiliate (),
				"default");

		}

		if (chatUser.getChatScheme () != null) {

			return affiliateHelper.findByCodeRequired (
				chatUser.getChatScheme (),
				"default");

		}

		return null;

	}

	@Override
	public
	Long getAffiliateId (
			@NonNull ChatUserRec chatUser) {

		AffiliateRec affiliate =
			getAffiliate (
				chatUser);

		return affiliate != null
			? affiliate.getId ()
			: null;

	}

	@Override
	public
	void logoff (
			@NonNull ChatUserRec chatUser,
			@NonNull Boolean automatic) {

		Transaction transaction =
			database.currentTransaction ();

		// log the user off

		chatUser

			.setOnline (
				false);

		// reset delivery method to sms, except for iphone users

		boolean iphone =
			chatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

		boolean token =
			chatUser.getJigsawToken () != null;

		if (! (iphone && token)) {

			chatUser

				.setDeliveryMethod (
					ChatMessageMethod.sms);

		}

		// finish their session

		for (
			ChatUserSessionRec chatUserSession
				: chatUser.getChatUserSessions ()
		) {

			if (chatUserSession.getEndTime () != null)
				continue;

			chatUserSession

				.setEndTime (
					transaction.now ())

				.setAutomatic (
					automatic);

		}

	}

	@Override
	public
	boolean deleted (
			@NonNull ChatUserRec chatUser) {

		return chatUser.getNumber () == null
			&& chatUser.getDeliveryMethod () == ChatMessageMethod.sms;

	}

	@Override
	public
	void scheduleAd (
			@NonNull ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		DateTimeZone timezone =
			getTimezone (
				chatUser);

		LocalDate today =
			transaction
				.now ()
				.toDateTime (timezone)
				.toLocalDate ();

		Instant midnightTonight =
			today
				.plusDays (1)
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		// start with their last action (or last join) date

		LocalDate startDate;

		if (chatUser.getLastJoin () != null) {

			startDate =
				localDate (
					chatUser.getLastJoin (),
					timezone);

		} else {

			startDate =
				localDate (
					chatUser.getLastAction (),
					timezone);

		}

		// pick a random time of day, from 10am to 8pm

		LocalTime timeOfDay =
			new LocalTime (
				10 + randomLogic.randomJavaInteger (10),
				randomLogic.randomJavaInteger (60),
				randomLogic.randomJavaInteger (60));

		// try and schedule first ad

		Instant firstAdTime =
			startDate

			.plusDays (
				toJavaIntegerRequired (
					chat.getAdTimeFirst () / 60 / 60 / 24))

			.toDateTime (
				timeOfDay,
				timezone)

			.toInstant ();

		if (firstAdTime.isAfter (
				midnightTonight)) {

			chatUser

				.setNextAd (
					firstAdTime);

			return;

		}

		// try and schedule subsequent ads

		for (
			int index = 0;
			index < chat.getAdCount ();
			index ++
		) {

			Instant nextAdTime =
				startDate

				.plusDays (
					toJavaIntegerRequired (
						chat.getAdTime () / 60 / 60 / 24 * index))

				.toDateTime (
					timeOfDay,
					timezone)

				.toInstant ();

			if (nextAdTime.isAfter (
					midnightTonight)) {

				chatUser

					.setNextAd (
						nextAdTime);

				return;

			}

		}

		// no more ads!

		chatUser

			.setNextAd (
				null);

	}

	@Override
	public
	boolean compatible (
			@NonNull Gender thisGender,
			@NonNull Orient thisOrient,
			@NonNull Optional<Long> thisCategoryId,
			@NonNull Gender thatGender,
			@NonNull Orient thatOrient,
			@NonNull Optional <Long> thatCategoryId) {

		if (

			isPresent (
				thisCategoryId)

			&& (

				isNotPresent (
					thatCategoryId)

				|| integerNotEqualSafe (
					thisCategoryId.get (),
					thatCategoryId.get ())

			)

		) {

			// category mismatch

			return false;

		} else if (
			enumEqualSafe (
				thisGender,
				thatGender)
		) {

			// same gender match

			return (
				thisOrient.doesSame ()
				&& thatOrient.doesSame ()
			);

		} else {

			// different gender match

			return (
				thisOrient.doesDifferent ()
				&& thatOrient.doesDifferent ()
			);

		}

	}

	@Override
	public
	boolean compatible (
			@NonNull ChatUserRec thisUser,
			@NonNull ChatUserRec thatUser) {

		if (

			isNull (
				thisUser.getGender ())

			|| isNull (
				thisUser.getOrient ())

			|| isNull (
				thatUser.getGender ())

			|| isNull (
				thatUser.getOrient ())

		) {

			return false;

		} else {

			return compatible (
				thisUser.getGender (),
				thisUser.getOrient (),
				thisUser.getCategory () != null
					? Optional.of (
						thisUser.getCategory ().getId ())
					: Optional.absent (),
				thatUser.getGender (),
				thatUser.getOrient (),
				thatUser.getCategory () != null
					? Optional.of (
						thatUser.getCategory ().getId ())
					: Optional.absent ());

		}

	}

	/**
	 * Works out the distance between thisUser and each of thoseUsers, returning
	 * up to the first numToFind of them.
	 */
	@Override
	public
	Collection <ChatUserRec> getNearestUsers (
			@NonNull ChatUserRec thisUser,
			@NonNull Collection <ChatUserRec> thoseUsers,
			@NonNull Long numToFind) {

		Collection <UserDistance> userDistances =
			getUserDistances (
				thisUser,
				thoseUsers);

		Set <ChatUserRec> ret =
			new HashSet<> ();

		for (UserDistance userDistance
				: userDistances) {

			ret.add (userDistance.user);

			if (ret.size () == numToFind)
				return ret;
		}

		return ret;

	}

	/**
	 * Returns a UserDistance representing the other user and their distance
	 * from thisUser, in ascending distance order, for every given otherUser.
	 */
	@Override
	public
	List<UserDistance> getUserDistances (
			@NonNull ChatUserRec thisUser,
			@NonNull Collection<ChatUserRec> otherUsers) {

		// process the list

		List<UserDistance> userDistances =
			new ArrayList<UserDistance> ();

		for (ChatUserRec thatUser
				: otherUsers) {

			if (thisUser.getLocationLongLat () == null)
				break;

			if (thatUser.getLocationLongLat () == null)
				continue;

			// work out the distance

			double miles =
				locatorLogic.distanceMiles (
					thisUser.getLocationLongLat (),
					thatUser.getLocationLongLat ());

			// and add them to the list

			UserDistance userDistance =
				new UserDistance ()

				.user (
					thatUser)

				.miles (
					miles);

			userDistances.add (
				userDistance);

		}

		// then sort the list

		Collections.sort (
			userDistances);

		return userDistances;

	}

	@Override
	public
	void adultVerify (
			@NonNull ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// work out times

		Instant nextAdultAdTime =
			transaction
				.now ()
				.plus (Duration.standardSeconds (
					chat.getAdultAdsTime ()));

		Instant adultExpiryTime =
			transaction
				.now ()
				.plus (Duration.standardDays (90));

		// update chat user

		chatUser

			.setAdultVerified (
				true)

			.setNextAdultAd (
				nextAdultAdTime)

			.setAdultExpiry (
				adultExpiryTime);

	}

	@Override
	public
	ChatUserRec createChatMonitor (
			@NonNull ChatRec chat) {

		Transaction transaction =
			database.currentTransaction ();

		return chatUserHelper.insert (
			chatUserHelper.createInstance ()

			.setChat (
				chat)

			.setCode (
				randomLogic.generateNumericNoZero (6))

			.setCreated (
				transaction.now ())

			.setType (
				ChatUserType.monitor)

		);

	}

	/**
	 * Updates the user's credit mode. This also increases or decreases their
	 * credit as necessary, as revoked credit is still counted in prePay mode,
	 * whereas it isn't otherwise.
	 */
	@Override
	public
	void creditModeChange (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserCreditMode newMode) {

		if (chatUser.getCreditMode () == ChatUserCreditMode.prePay) {

			chatUser

				.setCredit (
					chatUser.getCredit ()
					- chatUser.getCreditRevoked ());

		}

		chatUser
			.setCreditMode (newMode);

		if (chatUser.getCreditMode () == ChatUserCreditMode.prePay) {

			chatUser

				.setCredit (
					chatUser.getCredit ()
					+ chatUser.getCreditRevoked ());

		}

	}

	@Override
	public
	ChatUserImageRec setImage (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type,
			@NonNull MediaRec smallMedia,
			@NonNull MediaRec fullMedia,
			@NonNull Optional<MessageRec> message,
			boolean append) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// create the new chat user image

		ChatUserImageRec chatUserImage =
			chatUserImageHelper.insert (
				chatUserImageHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setMedia (
				smallMedia)

			.setFullMedia (
				fullMedia)

			.setTimestamp (
				transaction.now ())

			.setMessage (
				message.orNull ())

			.setStatus (
				ChatUserInfoStatus.moderatorPending)

			.setType (
				type)

			.setAppend (
				append));

		// create a queue item, if necessary

		if (chatUser.getQueueItem () == null) {

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					queueLogic.findQueue (chat, "user"),
					chatUser,
					chatUser,
					chatUser.getNumber ().getNumber (),
					getPrettyName (
						chatUser));

			chatUser

				.setQueueItem (
					queueItem);

		}

		return chatUserImage;

	}

	@Override
	public
	ChatUserImageRec setPhoto (
			@NonNull ChatUserRec chatUser,
			@NonNull MediaRec fullMedia,
			@NonNull Optional<MessageRec> message,
			boolean append) {

		// load

		byte[] fullData =
			fullMedia.getContent ().getData ();

		String fullMimeType =
			fullMedia.getMediaType ().getMimeType ();

		BufferedImage fullImage =
			mediaLogic.readImageRequired (
				fullData,
				fullMimeType);

		// resample

		BufferedImage smallImage =
			mediaLogic.resampleImageToFit (
				fullImage,
				320l,
				240l);

		// save

		byte[] smallData =
			mediaLogic.writeJpeg (
				smallImage,
				0.6f);

		MediaRec smallMedia =
			mediaLogic.createMediaFromImageRequired (
				smallData,
				"image/jpeg",
				chatUser.getCode () + ".jpg");

		// and delegate

		return setImage (
			chatUser,
			ChatUserImageType.image,
			smallMedia,
			fullMedia,
			message,
			append);

	}

	@Override
	public
	ChatUserImageRec setPhoto (
			@NonNull ChatUserRec chatUser,
			@NonNull byte[] data,
			@NonNull Optional <String> filenameOptional,
			@NonNull Optional <String> mimeType,
			@NonNull Optional <MessageRec> message,
			@NonNull Boolean append) {

		// create media

		String filename =
			filenameOptional.or (
				chatUser.getCode () + ".jpg");

		MediaRec fullMedia =
			mediaLogic.createMediaFromImageRequired (
				data,
				mimeType.or (
					"image/jpeg"),
				filename);

		// and delegate

		return setPhoto (
			chatUser,
			fullMedia,
			message,
			append);

	}

	@Override
	public
	Optional <ChatUserImageRec> setPhotoFromMessage (
			@NonNull ChatUserRec chatUser,
			@NonNull MessageRec message,
			@NonNull Boolean append) {

		return optionalMapRequired (

			findPhoto (
				message),

			media ->
				setPhoto (
					chatUser,
					media,
					Optional.of (
						message),
					append)

		);

	}

	@Override
	public
	Optional <MediaRec> findPhoto (
			@NonNull MessageRec message) {

		return optionalFromJava (
			message.getMedias ().stream ()

			.filter (
				media ->
					stringInSafe (
						media.getMediaType ().getMimeType (),
						"image/jpeg",
						"image/gif"))

			.findFirst ()

		);

	}

	@Override
	public
	void setVideo (
			@NonNull ChatUserRec chatUser,
			@NonNull MediaRec fullMedia,
			@NonNull MessageRec message,
			@NonNull Boolean append) {

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromVideoRequired (
				mediaLogic.videoConvertRequired (
					"3gpp",
					fullMedia.getContent ().getData ()),
				"video/3gpp",
				chatUser.getCode () + ".3gp");

		// and delegate

		setImage (
			chatUser,
			ChatUserImageType.video,
			newMedia,
			fullMedia,
			Optional.of (
				message),
			append);

	}

	@Override
	public
	void setVideo (
			@NonNull ChatUserRec chatUser,
			@NonNull byte[] data,
			@NonNull Optional <String> filenameOptional,
			@NonNull Optional <String> mimeTypeOptional,
			@NonNull MessageRec message,
			@NonNull Boolean append) {

		// default mime type

		String mimeType =
			mimeTypeOptional.or (
				"application/octet-stream");

		// default filename

		String filename;

		if (filenameOptional.isPresent ()) {

			filename =
				filenameOptional.get ();

		} else {

			MediaTypeRec mediaType =
				mediaTypeHelper.findByCodeRequired (
					GlobalId.root,
					mimeType);

			filename =
				stringFormat (
					"%s.%s",
					chatUser.getCode (),
					mediaType.getExtension ());

		}

		// create media

		MediaRec fullMedia =
			mediaLogic.createMediaFromVideoRequired (
				data,
				mimeType,
				filename);

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromVideoRequired (
				mediaLogic.videoConvertRequired ("3gpp", data),
				"video/3gpp",
				chatUser.getCode () + ".3gp");

		// and delegate

		setImage (
			chatUser,
			ChatUserImageType.video,
			newMedia,
			fullMedia,
			Optional.of (
				message),
			append);

	}

	@Override
	public
	void setAudio (
			@NonNull ChatUserRec chatUser,
			@NonNull byte[] data,
			@NonNull MessageRec message,
			@NonNull Boolean append) {

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromAudio (
				mediaLogic.videoConvertRequired ("mp3", data),
				"audio/mpeg",
				chatUser.getCode () + ".mp3");

		// and delegate

		setImage (
			chatUser,
			ChatUserImageType.audio,
			newMedia,
			null,
			Optional.of (
				message),
			append);

	}

	@Override
	public
	void setImage (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type,
			@NonNull byte[] data,
			@NonNull String filename,
			@NonNull String mimeType,
			@NonNull Optional<MessageRec> message,
			@NonNull Boolean append) {

		switch (type) {

		case image:

			setPhoto (
				chatUser,
				data,
				Optional.of (
					filename),
				Optional.of (
					mimeType),
				message,
				append);

			break;

		case video:

			setVideo (
				chatUser,
				data,
				Optional.of (filename),
				Optional.of (mimeType),
				null,
				append);

			break;

		case audio:

			setAudio (
				chatUser,
				data,
				null,
				append);

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Unknown chat user image type: %s (in ",
					type,
					"ChatLogicImpl.userSetImage)"));

		}

	}

	@Override
	public
	boolean setVideo (
			@NonNull ChatUserRec chatUser,
			@NonNull MessageRec message,
			@NonNull Boolean append) {

		for (MediaRec media
				: message.getMedias ()) {

			if (
				! mediaLogic.isVideo (
					media.getMediaType ().getMimeType ())
			) {
				continue;
			}

			setVideo (
				chatUser,
				media,
				message,
				append);

			return true;

		}

		return false;

	}

	@Override
	public
	boolean setPlace (
			@NonNull ChatUserRec chatUser,
			@NonNull String place,
			@NonNull Optional<MessageRec> message,
			@NonNull Optional<UserRec> user) {

		Transaction transaction =
			database.currentTransaction ();

		if (
			message.isPresent ()
			&& user.isPresent ()
		) {
			throw new IllegalArgumentException ();
		}

		ChatRec chat =
			chatUser.getChat ();

		// try and find the place

		Optional<GazetteerEntryRec> gazetteerEntryOptional =
			Optional.absent ();

		for (
			KeywordFinder.Match match
				: keywordFinder.find (place)
		) {

			gazetteerEntryOptional =
				gazetteerEntryHelper.findByCode (
					chat.getGazetteer (),
					match.simpleKeyword ());

			if (
				isPresent (
					gazetteerEntryOptional)
			) {
				break;
			}

		}

		if (
			isNotPresent (
				gazetteerEntryOptional)
		) {

			SliceRec slice =
				chat.getSlice ().getAdminEmail () != null
					? chat.getSlice ()
					: sliceHelper.findByCodeRequired (
						GlobalId.root,
						wbsConfig.defaultSlice ());

			if (slice.getAdminEmail () != null) {

				try {

					emailLogic.sendSystemEmail (

						ImmutableList.of (
							slice.getAdminEmail ()),

						stringFormat (
							"Chat user %s location not recognised: %s",
							stringFormat (
								"%s.%s.%s",
								chat.getSlice ().getCode (),
								chat.getCode (),
								chatUser.getCode ()),
							place),

						stringFormat (

							"Chat: %s\n",
							stringFormat (
								"%s.%s",
								chat.getSlice ().getCode (),
								chat.getCode ()),

							"Chat user: %s\n",
							chatUser.getCode (),

							"Message: %s\n",
							message.isPresent ()
								? message.get ().getId ()
								: "(via api)",

							"Location: %s\n",
							place,

							"Timestamp: %s\n",
							message.isPresent ()
								? message.get ().getCreatedTime ()
								: "(via api)"));

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						"logic",
						"ChatUserLogic.setPlace",
						exception,
						Optional.absent (),
						GenericExceptionResolution.ignoreWithLoggedWarning);

				}

			}

			return false;

		}

		GazetteerEntryRec gazetteerEntry =
			gazetteerEntryOptional.get ();

		// update the user

		if (gazetteerEntry.getLongLat () == null) {
			throw new NullPointerException ();
		}

		{

			if (
				! transaction.contains (
					chatUser)
			) {

				throw new IllegalStateException (
					stringFormat (
						"Chat user %s not in transaction",
						chatUser.getId ()));

			}

		}

		chatUser

			.setLocationPlace (
				gazetteerEntry.getName ())

			.setLocationPlaceLongLat (
				gazetteerEntry.getLongLat ())

			.setLocationLongLat (
				gazetteerEntry.getLongLat ())

			.setLocationBackupLongLat (
				gazetteerEntry.getLongLat ())

			.setLocationTime (
				transaction.now ());

		// create event

		if (message.isPresent ()) {

			eventLogic.createEvent (
				"chat_user_place_message",
				chatUser,
				gazetteerEntry,
				gazetteerEntry.getLongLat ().longitude (),
				gazetteerEntry.getLongLat ().latitude (),
				message.get ());

		} else if (user.isPresent ()) {

			eventLogic.createEvent (
				"chat_user_place_user",
				chatUser,
				gazetteerEntry,
				gazetteerEntry.getLongLat ().longitude (),
				gazetteerEntry.getLongLat ().latitude (),
				user.get ());

		} else {

			eventLogic.createEvent (
				"chat_user_place_api",
				chatUser,
				gazetteerEntry,
				gazetteerEntry.getLongLat ().longitude (),
				gazetteerEntry.getLongLat ().latitude ());

		}

		return true;

	}

	/**
	 * Checks if a given user has attempted one of the three methods of age
	 * verification:
	 *
	 * <ul>
	 * <li>responding "yes" to a message asking them (the old method)</li>
	 * <li>sending in their date of birth</li>
	 * <li>going through adult verfication</li>
	 * </ul>
	 *
	 * Note that in the case of DOB they may not be over 18, use the
	 * chatUserDobOk method for that.
	 *
	 * @param chatUser
	 *            ChatUserRec of the chat user to check
	 * @return true if they meet any of the criteria
	 * @see #chatUserDobOk
	 */
	@Override
	public
	boolean gotDob (
			@NonNull ChatUserRec chatUser) {

		return /*chatUser.getAdultVerified ()
			|| chatUser.getAgeChecked ()
			||*/ chatUser.getDob () != null;

	}

	/**
	 * Checks if a given chat user has given adequate evidence they are over 18:
	 *
	 * <ul>
	 * <li>responding "yes" to a message asking them (the old method)</li>
	 * <li>sending in their date of birth which shows them to be at least 18</li>
	 * <li>going through adult verfication</li>
	 * </ul>
	 *
	 * @param chatUser
	 * @return
	 */
	@Override
	public
	boolean dobOk (
			@NonNull ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

		DateTimeZone timezone =
			getTimezone (
				chatUser);

		if (chatUser.getAgeChecked ())
			return true;

		if (chatUser.getDob () == null)
			return true;

		return notLessThan (
			getAgeInYears (
				chatUser,
				transaction.now ()),
			18l);

	}

	/**
	 * Sets a chat user's scheme. This does nothing if they are already on
	 * another scheme, and will automatically give them any free credit they are
	 * due.
	 *
	 * @param chatUser
	 *            chat user to set the scheme of
	 * @param chatScheme
	 *            chat scheme to set
	 */
	@Override
	public
	void setScheme (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatSchemeRec chatScheme) {

		// do nothing if already set

		if (chatUser.getChatScheme () != null)
			return;

		// check for mappings

		Set<ChatSchemeMapRec> chatSchemeMaps =
			chatScheme.getChatSchemeMaps ();

		if (! chatSchemeMaps.isEmpty ()) {

			String number =
				chatUser.getNumber ().getNumber ();

			for (
				ChatSchemeMapRec chatSchemeMap
					: chatSchemeMaps
			) {

				String prefix =
					chatSchemeMap.getPrefix ();

				if (
					stringNotEqualSafe (
						prefix,
						number.subSequence (0, prefix.length ()))
				) {
					continue;
				}

				chatScheme =
					chatSchemeMap.getTargetChatScheme ();

				break;

			}

		}

		// just set it

		chatUser

			.setChatScheme (
				chatScheme)

			.setOperatorLabel (
				chatScheme.getOperatorLabel ())

			.setCredit (
				chatUser.getCredit ()
				+ chatScheme.getInitialCredit ());

	}

	/**
	 * Sets a chat user's affiliate (and scheme). This will automatically give
	 * them any free credit they are due and will not do anything if they are
	 * already set to a different affiliate or scheme.
	 *
	 * @param chatUser
	 *            the chat user to update
	 * @param chatAffiliate
	 *            the chat affiliate to set to
	 */
	@Override
	public
	void setAffiliate (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatAffiliateRec chatAffiliate,
			@NonNull Optional<MessageRec> message) {

		ChatSchemeRec chatScheme =
			chatAffiliate.getChatScheme ();

		// set category

		if (

			isNotNull (
				chatAffiliate.getCategory ())

			&& referenceNotEqualSafe (
				chatUser.getCategory (),
				chatAffiliate.getCategory ())

		) {

			chatUser

				.setCategory (
					chatAffiliate.getCategory ());

			if (
				isPresent (
					message)
			) {

				eventLogic.createEvent (
					"chat_user_category_message",
					chatUser,
					chatUser.getCategory (),
					message.get ());

			} else {

				eventLogic.createEvent (
					"chat_user_category_api",
					chatUser,
					chatUser.getCategory ());

			}

		}

		// user has no scheme or affiliate

		if (
			chatUser.getChatAffiliate () == null
			&& chatUser.getChatScheme () == null
		) {

			chatUser

				.setChatAffiliate (
					chatAffiliate)

				.setChatScheme (
					chatScheme)

				.setOperatorLabel (
					chatScheme.getOperatorLabel ())

				.setCredit (
					chatUser.getCredit ()
					+ chatScheme.getInitialCredit ());

			return;

		}

		// user is non-affiliated but on the same scheme

		if (
			chatUser.getChatAffiliate () == null
			&& chatUser.getChatScheme () == chatScheme
		) {

			chatUser

				.setChatAffiliate (
					chatAffiliate);

			return;
		}

		// do nothing

		return;

	}

	@Override
	public
	boolean valid (
			@NonNull ChatUserRec chatUser) {

		if (
			chatUser.getType () == ChatUserType.user
			&& chatUser.getNumber () == null
		) {
			return false;
		}

		return true;

	}

	@Override
	public
	ChatUserImageRec chatUserPendingImage (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type) {

		ChatUserImageRec ret = null;

		for (
			ChatUserImageRec chatUserImage
				: chatUser.getChatUserImages ()
		) {

			if (
				enumNotEqualSafe (
					chatUserImage.getType (),
					type)
			) {
				continue;
			}

			if (chatUserImage.getStatus ()
					!= ChatUserInfoStatus.moderatorPending)
				continue;

			if (

				isNull (
					ret)

				|| earlierThan (
					ret.getTimestamp (),
					chatUserImage.getTimestamp ())

			) {

				ret =
					chatUserImage;

			}

		}

		return ret;

	}

	@Override
	public
	ChatUserImageType imageTypeForMode (
			@NonNull PendingMode mode) {

		switch (mode) {

		case image:
			return ChatUserImageType.image;

		case video:
			return ChatUserImageType.video;

		case audio:
			return ChatUserImageType.audio;

		default:

			throw new RuntimeException (
				stringFormat (
					"Not an image mode: %s",
					mode));

		}

	}

	@Override
	public
	String getBrandName (
			@NonNull ChatUserRec chatUser) {

		ChatAffiliateRec chatAffiliate =
			chatUser.getChatAffiliate ();

		if (
			chatAffiliate != null
			&& chatAffiliate.getBrandName () != null
		) {
			return chatAffiliate.getBrandName ();
		}

		ChatRec chat =
			chatUser.getChat ();

		return chat.getDefaultBrandName ();

	}

	@Override
	public
	DateTimeZone getTimezone (
			@NonNull ChatUserRec chatUser) {

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		if (chatScheme != null) {

			return timeFormatter.timezone (
				chatScheme.getTimezone ());

		}

		ChatRec chat =
			chatUser.getChat ();

		return timeFormatter.timezone (
			chat.getTimezone ());

	}

	@Override
	public
	List<ChatUserImageRec> getChatUserImageListByType (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type) {

		switch (type) {

			case image:
				return chatUser.getChatUserImageList ();

			case video:
				return chatUser.getChatUserVideoList ();

			case audio:
				return chatUser.getChatUserAudioList ();

			default:
				throw new RuntimeException ();

		}

	}

	@Override
	public
	ChatUserImageRec getMainChatUserImageByType (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type) {

		switch (type) {

			case image:
				return chatUser.getMainChatUserImage ();

			case video:
				return chatUser.getMainChatUserVideo ();

			case audio:
				return chatUser.getMainChatUserAudio ();

			default:
				throw new RuntimeException ();

		}

	}

	@Override
	public
	void setMainChatUserImageByType (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserImageType type,
			@NonNull Optional<ChatUserImageRec> chatUserImage) {

		switch (type) {

			case image:

				chatUser

					.setMainChatUserImage (
						chatUserImage.orNull ());

				break;

			case video:

				chatUser

					.setMainChatUserVideo (
						chatUserImage.orNull ());

				break;

			case audio:

				chatUser

					.setMainChatUserAudio (
						chatUserImage.orNull ());

				break;

			default:

				throw new RuntimeException ();

		}

	}

	@Override
	public
	String getPrettyName (
			@NonNull ChatUserRec chatUser) {

		if (chatUser.getName () == null) {
			return chatUser.getCode ();
		} else {
			return chatUser.getCode () + " " + chatUser.getName ();
		}

	}

	@Override
	public
	boolean likes (
			@NonNull ChatUserRec chatUser,
			@NonNull Gender otherGender) {

		if (chatUser.getOrient () == Orient.bi) {
			return true;
		}

		if (otherGender == Gender.male) {

			if (
				chatUser.getGender () == Gender.male
				&& chatUser.getOrient () == Orient.gay
			) {
				return true;
			}

			if (
				chatUser.getGender () == Gender.female
				&& chatUser.getOrient () == Orient.straight
			) {
				return true;
			}

		}

		if (otherGender == Gender.female) {

			if (
				chatUser.getGender () == Gender.female
				&& chatUser.getOrient () == Orient.gay
			) {
				return true;
			}

			if (
				chatUser.getGender () == Gender.male
				&& chatUser.getOrient () == Orient.straight
			) {
				return true;
			}

		}

		return false;

	}

	@Override
	public 
	long getAgeInYears (
			@NonNull ChatUserRec chatUser,
			@NonNull Instant now) {

		return calculateAgeInYears (
			chatUser.getDob (),
			now,
			getTimezone (
				chatUser));

	}

}
