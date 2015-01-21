package wbs.apn.chat.user.core.logic;

import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.DateTimeZone;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageRec;

import com.google.common.base.Optional;

public
interface ChatUserLogic {

	AffiliateRec getAffiliate (
			ChatUserRec chatUser);

	Integer getAffiliateId (
			ChatUserRec chatUser);

	void logoff (
			ChatUserRec chatUser,
			boolean automatic);

	boolean deleted (
			ChatUserRec chatUser);

	void scheduleAd (
			ChatUserRec chatUser);

	boolean compatible (
			Gender gender1,
			Orient orient1,
			Gender gender2,
			Orient orient2);

	boolean compatible (
			ChatUserRec user1,
			ChatUserRec user2);

	Collection<ChatUserRec> getNearestUsers (
			ChatUserRec thisUser,
			Collection<ChatUserRec> thoseUsers,
			int numToFind);

	List<UserDistance> getUserDistances (
			ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers);

	/**
	 * Aggregate for a ChatUser and an integer distance. Implements
	 * Object.hashCode and is Comparable--ascending by distance.
	 */
	@Accessors (fluent = true)
	@Data
	@EqualsAndHashCode
	public static
	class UserDistance
		implements Comparable<UserDistance> {

		ChatUserRec user;
		double miles;

		@Override
		public
		int compareTo (
				UserDistance other) {

			return new CompareToBuilder ()

				.append (
					miles,
					other.miles)

				.append (
					user,
					other.user)

				.toComparison ();

		}

	}

	void adultVerify (
			ChatUserRec chatUser);

	void monitorCap (
			ChatUserRec chatUser);

	ChatUserRec createChatMonitor (
			ChatRec chat);

	/**
	 * Updates the user's credit mode. This also increases or decreases their
	 * credit as necessary, as revoked credit is still counted in prePay mode,
	 * whereas it isn't otherwise.
	 */
	void creditModeChange (
			ChatUserRec chatUser,
			ChatUserCreditMode newMode);

	ChatUserImageRec setImage (
			ChatUserRec chatUser,
			ChatUserImageType type,
			MediaRec smallMedia,
			MediaRec fullMedia,
			Optional<MessageRec> message,
			boolean append);

	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			Optional<MessageRec> message,
			boolean append);

	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			byte[] data,
			Optional<String> filename,
			Optional<String> mimeType,
			Optional<MessageRec> message,
			boolean append);

	ChatUserImageRec setPhotoFromMessage (
			ChatUserRec chatUser,
			MessageRec message,
			boolean append);

	void setVideo (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			MessageRec message,
			boolean append);

	void setVideo (
			ChatUserRec chatUser,
			byte[] data,
			Optional<String> filename,
			Optional<String> mimeType,
			MessageRec message,
			boolean append);

	void setAudio (
			ChatUserRec chatUser,
			byte[] data,
			MessageRec message,
			boolean append);

	void setImage (
			ChatUserRec chatUser,
			ChatUserImageType type,
			byte[] data,
			String filename,
			String mimeType,
			Optional<MessageRec> message,
			boolean append);

	boolean setVideo (
			ChatUserRec chatUser,
			MessageRec message,
			boolean append);

	boolean setPlace (
			ChatUserRec chatUser,
			String place,
			Optional<MessageRec> message);

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
	boolean gotDob (
			ChatUserRec chatUser);

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
	boolean dobOk (
			ChatUserRec chatUser);

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
	void setScheme (
			ChatUserRec chatUser,
			ChatSchemeRec chatScheme);

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
	void setAffiliate (
			ChatUserRec chatUser,
			ChatAffiliateRec chatAffiliate);

	MediaRec findPhoto (
			MessageRec message);

	boolean valid (
			ChatUserRec chatUser);

	ChatUserImageRec chatUserPendingImage (
			ChatUserRec chatUser,
			ChatUserImageType type);

	@Accessors (fluent = true)
	public
	enum PendingMode {

		none,
		name ("reject_name", null, null),
		info ("reject_info", null, null),
		image ("reject_image", "set_photo", "chatUserImageList"),
		video ("reject_image", "video_set", "chatUserVideoList"),
		audio ("reject_image", "audio_set", "chatUserAudioList");

		@Getter String rejectType;
		@Getter String commandCode;
		@Getter String listProperty;

		PendingMode () {

			this (
				null,
				null,
				null);

		}

		PendingMode (
				String rejectType) {

			this (
				rejectType,
				null,
				null);

		}

		PendingMode (
				String rejectType,
				String commandCode,
				String listProperty) {

			this.rejectType = rejectType;
			this.commandCode = commandCode;
			this.listProperty = listProperty;

		}

	}

	ChatUserImageType imageTypeForMode (
			PendingMode mode);

	String getBrandName (
			ChatUserRec chatUser);

	DateTimeZone timezone (
			ChatUserRec chatUser);

}
