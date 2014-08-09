package wbs.apn.chat.user.core.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeMapRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.email.logic.EmailLogic;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeObjectHelper;
import wbs.platform.media.model.MediaTypeRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.number.core.logic.NumberLogic;

@SingletonComponent ("chatUserLogic")
public
class ChatUserLogicImpl
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
	EmailLogic emailLogic;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	GazetteerEntryObjectHelper gazetteerEntryHelper;

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
	Random random;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	AffiliateRec getAffiliate (
			ChatUserRec chatUser) {

		if (chatUser.getChatAffiliate () != null) {

			return affiliateHelper.findByCode (
				chatUser.getChatAffiliate (),
				"default");

		}

		if (chatUser.getChatScheme () != null) {

			return affiliateHelper.findByCode (
				chatUser.getChatScheme (),
				"default");

		}

		return null;
	}

	@Override
	public
	Integer getAffiliateId (
			ChatUserRec chatUser) {

		AffiliateRec affiliate =
			getAffiliate (chatUser);

		return affiliate != null ?
			affiliate.getId () : null;
	}

	@Override
	public
	void setAffiliateId (
			ReceivedMessage receivedMessage,
			ChatUserRec chatUser) {

		Integer affiliateId =
			getAffiliateId (chatUser);

		if (affiliateId != null)
			receivedMessage.setAffiliateId (affiliateId);
	}

	@Override
	public
	void logoff (
			ChatUserRec chatUser,
			boolean automatic) {

		Date now =
			new Date ();

		// log the user off

		chatUser.setOnline (false);

		// reset delivery method to sms, except for iphone users

		boolean iphone =
			chatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

		boolean token =
			chatUser.getJigsawToken () != null;

		if (! (iphone && token))
			chatUser.setDeliveryMethod (ChatMessageMethod.sms);

		// finish their session

		for (ChatUserSessionRec chatUserSession
				: chatUser.getChatUserSessions ()) {

			if (chatUserSession.getEndTime () != null)
				continue;

			chatUserSession
				.setEndTime (now)
				.setAutomatic (automatic);

		}

	}

	@Override
	public boolean deleted (
			ChatUserRec chatUser) {

		return chatUser.getNumber () == null
			&& chatUser.getDeliveryMethod () == ChatMessageMethod.sms;

	}

	@Override
	public
	void scheduleAd (
			ChatUserRec chatUser) {

		ChatRec chat =
			chatUser.getChat ();

		Calendar calendar =
			Calendar.getInstance ();

		// earliest we will send ad is from midnight tonight

		calendar.setTime (new Date ());

		calendar.set (Calendar.HOUR_OF_DAY, 0);
		calendar.set (Calendar.MINUTE, 0);
		calendar.set (Calendar.SECOND, 0);
		calendar.set (Calendar.MILLISECOND, 0);

		calendar.add (Calendar.DATE, 1);

		Date midnight =
			calendar.getTime ();

		// start with their last action (or last join) time

		if (chatUser.getLastJoin () != null) {

			calendar.setTime (chatUser.getLastJoin ());

		} else {

			calendar.setTime (chatUser.getLastAction ());

		}

		// randomise time of day

		calendar.set (
			Calendar.HOUR_OF_DAY,
			10 + random.nextInt (10));

		// try and schedule first ad

		calendar.add (
			Calendar.SECOND,
			chat.getAdTimeFirst ());

		if (calendar.getTime ().getTime () > midnight.getTime ()) {
			chatUser.setNextAd (calendar.getTime ());
			return;
		}

		// try and schedule subsequent ads

		for (int i = 0; i < chat.getAdCount (); i++) {

			calendar.add (
				Calendar.SECOND,
				chat.getAdTime ());

			if (calendar.getTime ().getTime () > midnight.getTime ()) {
				chatUser.setNextAd (calendar.getTime ());
				return;
			}
		}

		// no more ads!

		chatUser.setNextAd (null);

	}

	/**
	 * Checks if the two gender/orient pairs are both potential matches.
	 *
	 * @param gender1
	 *            the first gender
	 * @param orient1
	 *            the first orient
	 * @param gender2
	 *            the second gender
	 * @param orient2
	 *            the second orient
	 * @return true if they are
	 */
	@Override
	public boolean compatible (
			Gender gender1,
			Orient orient1,
			Gender gender2,
			Orient orient2) {

		if (gender1 == null || orient1 == null
			|| gender2 == null || orient2 == null)
			return false;

		if (gender1 == gender2) {

			return orient1.doesSame ()
				&& orient2.doesSame ();

		} else {

			return orient1.doesDifferent ()
				&& orient2.doesDifferent ();

		}

	}

	/**
	 * Returns true if and only if the two users are both potential matches,
	 * based on orient and gender.
	 *
	 * @param user1
	 *            the first user
	 * @param user2
	 *            the second user
	 * @return true if they are
	 */
	@Override
	public boolean compatible (
			ChatUserRec user1,
			ChatUserRec user2) {

		return compatible (
			user1.getGender (),
			user1.getOrient (),
			user2.getGender (),
			user2.getOrient ());

	}

	/**
	 * Works out the distance between thisUser and each of thoseUsers, returning
	 * up to the first numToFind of them.
	 */
	@Override
	public Collection<ChatUserRec> getNearestUsers (
			ChatUserRec thisUser,
			Collection<ChatUserRec> thoseUsers,
			int numToFind) {

		Collection<UserDistance> userDistances =
			getUserDistances (
				thisUser,
				thoseUsers);

		Set<ChatUserRec> ret =
			new HashSet<ChatUserRec> ();

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
			ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers) {

		// process the list

		List<UserDistance> userDistances =
			new ArrayList<UserDistance> ();

		for (ChatUserRec thatUser
				: otherUsers) {

			if (thisUser.getLocLongLat () == null)
				break;

			if (thatUser.getLocLongLat () == null)
				continue;

			// work out the distance

			double miles =
				locatorLogic.distanceMiles (
					thisUser.getLocLongLat (),
					thatUser.getLocLongLat ());

			// and add them to the list

			UserDistance userDistance =
				new UserDistance ()
					.user (thatUser)
					.miles (miles);

			userDistances.add (userDistance);

		}

		// then sort the list

		Collections.sort (userDistances);

		return userDistances;

	}

	@Override
	public
	void adultVerify (
			ChatUserRec chatUser) {

		ChatRec chat =
			chatUser.getChat ();

		// mark as adult verified

		chatUser.setAdultVerified (true);

		// set next adult ad

		Calendar cal1 =
			Calendar.getInstance ();

		cal1.add (
			Calendar.SECOND,
			chat.getAdultAdsTime ());

		chatUser.setNextAdultAd (
			cal1.getTime ());

		// set next adult expiry

		Calendar cal2 =
			new GregorianCalendar ();

		cal2.add (
			Calendar.MONTH,
			3);

		chatUser.setAdultExpiry (
			cal2.getTime ());

	}

	@Override
	public
	void monitorCap (
			ChatUserRec chatUser) {

		int number =
			random.nextInt (100);

		if (number < 10)
			chatUser.setMonitorCap (number);

	}

	@Override
	public
	ChatUserRec createChatMonitor (
			ChatRec chat) {

		return objectManager.insert (
			new ChatUserRec ()

			.setChat (
				chat)

			.setType (
				ChatUserType.monitor)

			.setCode (
				chatUserHelper.generateCode (chat)));

	}

	/**
	 * Updates the user's credit mode. This also increases or decreases their
	 * credit as necessary, as revoked credit is still counted in prePay mode,
	 * whereas it isn't otherwise.
	 */
	@Override
	public
	void creditModeChange (
			ChatUserRec chatUser,
			ChatUserCreditMode newMode) {

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
			ChatUserRec chatUser,
			ChatUserImageType type,
			MediaRec smallMedia,
			MediaRec fullMedia,
			MessageRec message,
			boolean append) {

		ChatRec chat =
			chatUser.getChat ();

		// create the new chat user image

		ChatUserImageRec chatUserImage =
			chatUserImageHelper.insert (
				new ChatUserImageRec ()
					.setChatUser (chatUser)
					.setMedia (smallMedia)
					.setFullMedia (fullMedia)
					.setTimestamp (new Date ())
					.setMessage (message)
					.setStatus (ChatUserInfoStatus.moderatorPending)
					.setType (type)
					.setAppend (append));

		// create a queue item, if necessary

		if (chatUser.getQueueItem () == null) {

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					queueLogic.findQueue (chat, "user"),
					chatUser,
					chatUser,
					chatUser.getNumber ().getNumber (),
					chatUser.getPrettyName ());

			chatUser
				.setQueueItem (queueItem);

		}

		return chatUserImage;
	}

	@Override
	public
	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			MessageRec message,
			boolean append) {

		// load

		byte[] fullData =
			fullMedia.getContent ().getData ();

		String fullMimeType =
			fullMedia.getMediaType ().getMimeType ();

		BufferedImage fullImage =
			mediaLogic.readImage (fullData, fullMimeType);

		// resample

		BufferedImage smallImage =
			mediaLogic.resampleImage (fullImage, 320, 240);

		// save

		byte[] smallData =
			mediaLogic.writeJpeg (smallImage, 0.6f);

		MediaRec smallMedia =
			mediaLogic.createMediaFromImage (
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
	public ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			byte[] data,
			MessageRec message,
			boolean append) {

		// create media

		String filename =
			chatUser.getCode () + ".jpg";

		MediaRec fullMedia =
			mediaLogic.createMediaFromImage (
				data,
				"image/jpeg",
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
	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			MessageRec message,
			boolean append) {

		MediaRec media =
			findPhoto (message);

		if (media == null)
			return null;

		return setPhoto (
			chatUser,
			media,
			message,
			append);

	}

	@Override
	public
	MediaRec findPhoto (
			MessageRec message) {

		// look for a valid jpeg or gif

		for (MediaRec media
				: message.getMedias ()) {

			if (! in (media.getMediaType ().getMimeType (),
					"image/jpeg",
					"image/gif"))
				continue;

			return media;

		}

		return null;

	}

	@Override
	public
	void setVideo (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			MessageRec message,
			boolean append) {

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromVideo (
				mediaLogic.videoConvert (
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
			message,
			append);

	}

	@Override
	public
	void setVideo (
			ChatUserRec chatUser,
			byte[] data,
			String filename,
			String mimeType,
			MessageRec message,
			boolean append) {

		// default mime type

		if (mimeType == null)
			mimeType = "application/octet-stream";

		// default filename

		if (filename == null) {

			MediaTypeRec mediaType =
				mediaTypeHelper.findByCode (
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
			mediaLogic.createMediaFromVideo (
				data,
				mimeType,
				filename);

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromVideo (
				mediaLogic.videoConvert ("3gpp", data),
				"video/3gpp",
				chatUser.getCode () + ".3gp");

		// and delegate
		setImage (
			chatUser,
			ChatUserImageType.video,
			newMedia,
			fullMedia,
			message,
			append);
	}

	@Override
	public
	void setAudio (
			ChatUserRec chatUser,
			byte[] data,
			MessageRec message,
			boolean append) {

		// resample

		MediaRec newMedia =
			mediaLogic.createMediaFromAudio (
				mediaLogic.videoConvert ("mp3", data),
				"audio/mpeg",
				chatUser.getCode () + ".mp3");

		// and delegate

		setImage (
			chatUser,
			ChatUserImageType.audio,
			newMedia,
			null,
			message,
			append);

	}

	@Override
	public
	void setImage (
			ChatUserRec chatUser,
			ChatUserImageType type,
			byte[] data,
			String filename,
			String mimeType,
			MessageRec message,
			boolean append) {

		switch (type) {

		case image:

			setPhoto (
				chatUser,
				data,
				null,
				true);

			break;

		case video:

			setVideo (
				chatUser,
				data,
				filename,
				mimeType,
				null,
				true);

			break;

		case audio:

			setAudio (
				chatUser,
				data,
				null,
				true);

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
			ChatUserRec chatUser,
			MessageRec message,
			boolean append) {

		for (MediaRec media
				: message.getMedias ()) {

			if (! MediaLogic.videoTypes.contains (
					media.getMediaType ().getMimeType ()))
				continue;

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
			ChatUserRec chatUser,
			String place,
			MessageRec message) {

		ChatRec chat =
			chatUser.getChat ();

		// try and find the place

		GazetteerEntryRec gazetteerEntry = null;

		for (KeywordFinder.Match match
				: KeywordFinder.find (place)) {

			gazetteerEntry =
				gazetteerEntryHelper.findByCode (
					chat.getGazetteer (),
					match.simpleKeyword ());

			if (gazetteerEntry != null)
				break;

		}

		if (gazetteerEntry == null) {

			SliceRec slice =
				chat.getSlice ().getAdminEmail () != null
					? chat.getSlice ()
					: sliceHelper.findByCode (
						GlobalId.root,
						wbsConfig.defaultSlice ());

			if (slice.getAdminEmail () != null) {

				try {

					emailLogic.sendEmail (
						slice.getAdminEmail (),
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
							message != null
								? message.getId ()
								: "(via api)",

							"Location: %s\n",
							place,

							"Timestamp: %s\n",
							message.getCreatedTime ()));

				} catch (Exception exception) {

					exceptionLogic.logException (
						"logic",
						"ChatUserLogic.setPlace",
						exception,
						null,
						false);

				}

			}

			return false;

		}

		// update the user

		chatUser

			.setLocPlace (
				gazetteerEntry.getName ())

			.setLocPlaceLongLat (
				gazetteerEntry.getLongLat ())

			.setLocLongLat (
				gazetteerEntry.getLongLat ())

			.setLocTime (
				new Date ());

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
	boolean
	gotDob (
			ChatUserRec chatUser) {

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
			ChatUserRec chatUser) {

		if (chatUser.getAdultVerified ()
				|| chatUser.getAgeChecked ())
			return true;

		if (chatUser.getDob () == null)
			return true;

		Calendar calendar =
			Calendar.getInstance ();

		calendar.add (Calendar.YEAR, -18);

		return chatUser.getDob ().toDate ().getTime ()
			<= calendar.getTime ().getTime ();

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
			ChatUserRec chatUser,
			ChatSchemeRec chatScheme) {

		// do nothing if already set

		if (chatUser.getChatScheme () != null)
			return;

		// check for mappings

		Set<ChatSchemeMapRec> chatSchemeMaps =
			chatScheme.getChatSchemeMaps ();

		if (! chatSchemeMaps.isEmpty ()) {

			String number =
				chatUser.getNumber ().getNumber ();

			for (ChatSchemeMapRec chatSchemeMap
					: chatSchemeMaps) {

				String prefix =
					chatSchemeMap.getPrefix ();

				if (! equal (
						prefix,
						number.subSequence (0, prefix.length ())))
					continue;

				chatScheme =
					chatSchemeMap.getTargetChatScheme ();

				break;

			}

		}

		// just set it

		chatUser

			.setChatScheme (
				chatScheme)

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
			ChatUserRec chatUser,
			ChatAffiliateRec chatAffiliate) {

		ChatSchemeRec chatScheme =
			chatAffiliate.getChatScheme ();

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
				.setChatAffiliate (chatAffiliate);

			return;
		}

		// do nothing

		return;

	}

	@Override
	public
	boolean valid (
			ChatUserRec chatUser) {

		if (chatUser == null)
			return false;

		if (chatUser.getType () == ChatUserType.user
				&& chatUser.getNumber () == null)
			return false;

		return true;

	}

	@Override
	public
	ChatUserImageRec chatUserPendingImage (
			ChatUserRec chatUser,
			ChatUserImageType type) {

		ChatUserImageRec ret = null;

		for (ChatUserImageRec chatUserImage
				: chatUser.getChatUserImages ()) {

			if (! equal (
					chatUserImage.getType (),
					type))
				continue;

			if (chatUserImage.getStatus ()
					!= ChatUserInfoStatus.moderatorPending)
				continue;

			if (ret == null
					|| ret.getTimestamp ().getTime ()
						< chatUserImage.getTimestamp ().getTime ())
				ret = chatUserImage;

		}

		return ret;

	}

	@Override
	public
	ChatUserImageType imageTypeForMode (
			PendingMode mode) {

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
			ChatUserRec chatUser) {

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

}
