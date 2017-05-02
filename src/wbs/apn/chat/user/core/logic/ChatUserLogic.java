package wbs.apn.chat.user.core.logic;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;

public
interface ChatUserLogic {

	AffiliateRec getAffiliate (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	Long getAffiliateId (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void logoff (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Boolean automatic);

	boolean deleted (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void scheduleAd (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	boolean compatible (
			Transaction parentTransaction,
			Gender thisGender,
			Orient thisOrient,
			Optional <Long> thisCategoryId,
			Gender thatGender,
			Orient thatOrient,
			Optional <Long> thatCategoryId);

	boolean compatible (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			ChatUserRec thatUser);

	Collection <ChatUserRec> getNearestUsers (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Collection <ChatUserRec> thoseUsers,
			Long numToFind);

	List <UserDistance> getUserDistances (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers);

	void adultVerify (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	ChatUserRec createChatMonitor (
			Transaction parentTransaction,
			ChatRec chat);

	void creditModeChange (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatUserCreditMode newMode);

	ChatUserImageRec setImage (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatUserImageType type,
			MediaRec smallMedia,
			MediaRec fullMedia,
			Optional <MessageRec> message,
			Boolean append);

	ChatUserImageRec setPhoto (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MediaRec fullMedia,
			Optional <MessageRec> message,
			Boolean append);

	ChatUserImageRec setPhoto (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			byte[] data,
			Optional <String> filename,
			Optional <String> mimeType,
			Optional <MessageRec> message,
			Boolean append);

	Optional <ChatUserImageRec> setPhotoFromMessage (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MessageRec message,
			Boolean append);

	void setVideo (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MediaRec fullMedia,
			MessageRec message,
			Boolean append);

	void setVideo (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			byte[] data,
			Optional<String> filename,
			Optional<String> mimeType,
			MessageRec message,
			Boolean append);

	void setAudio (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			byte[] data,
			MessageRec message,
			Boolean append);

	void setImage (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatUserImageType type,
			byte[] data,
			String filename,
			String mimeType,
			Optional <MessageRec> message,
			Boolean append);

	boolean setVideo (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MessageRec message,
			Boolean append);

	boolean setPlace (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			String place,
			Optional <MessageRec> message,
			Optional <UserRec> user);

	boolean gotDob (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	boolean dobOk (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void setScheme (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatSchemeRec chatScheme);

	void setAffiliate (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatAffiliateRec chatAffiliate,
			Optional <MessageRec> message);

	Optional <MediaRec> findPhoto (
			Transaction parentTransaction,
			MessageRec message);

	boolean valid (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	ChatUserImageRec chatUserPendingImage (
			Transaction parentTransaction,
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

	DateTimeZone getTimezone (
			ChatUserRec chatUser);

	List<ChatUserImageRec> getChatUserImageListByType (
			ChatUserRec chatUser,
			ChatUserImageType type);

	ChatUserImageRec getMainChatUserImageByType (
			ChatUserRec chatUser,
			ChatUserImageType type);

	void setMainChatUserImageByType (
			ChatUserRec chatUser,
			ChatUserImageType type,
			Optional<ChatUserImageRec> chatUserImage);

	String getPrettyName (
			ChatUserRec chatUser);

	boolean likes (
			ChatUserRec chatUser,
			Gender otherGender);

	long getAgeInYears (
			ChatUserRec chatUser,
			Instant now);

	@Accessors (fluent = true)
	@Data
	@EqualsAndHashCode
	public static
	class UserDistance
		implements Comparable <UserDistance> {

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

}
