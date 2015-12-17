package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.underscoreToHyphen;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.collect.ImmutableList;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.applications.imchat.model.ImChatCustomerDetailValueRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;

@SingletonComponent ("imChatApiLogic")
public
class ImChatApiLogicImplementation
	implements ImChatApiLogic {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	ImChatPricePointData pricePointData (
			@NonNull ImChatPricePointRec pricePoint) {

		ImChatRec imChat =
			pricePoint.getImChat ();

		return new ImChatPricePointData ()

			.code (
				underscoreToHyphen (
					pricePoint.getCode ()))

			.name (
				pricePoint.getName ())

			.description (
				pricePoint.getDescription ())

			.priceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) pricePoint.getPrice ()))

			.valueString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) pricePoint.getValue ()));

	}

	@Override
	public
	ImChatProfileData profileData (
			@NonNull ImChatProfileRec profile) {

		if (profile.getDeleted ()) {
			throw new RuntimeException ();
		}

		MediaRec image =
			profile.getProfileImage ();

		ContentRec content =
			image.getContent ();

		Integer hash =
			Math.abs (
				content.getHash ());

		int resizedWidth = 98;

		int resizedHeight =
			image.getHeight () * resizedWidth / image.getWidth ();

		return new ImChatProfileData ()

			.code (
				underscoreToHyphen (
					profile.getCode ()))

			.name (
				profile.getPublicName ())

			.description (
				profile.getPublicDescription ())

			.descriptionShort (
				profile.getPublicDescriptionShort ())

			.thumbnailImageLink (
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/im-chat-media/%u",
					image.getId (),
					"/%u",
					hash,
					"/thumbnail.jpg"))

			.thumbnailImageWidth (
				resizedWidth)

			.thumbnailImageHeight (
				resizedHeight)

			.miniatureImageLink (
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/im-chat-media/%u",
					image.getId (),
					"/%u",
					hash,
					"/miniature.jpg"))

			.miniatureImageWidth (
				24)

			.miniatureImageHeight (
				24);

	}

	@Override
	public
	ImChatCustomerData customerData (
			@NonNull ImChatCustomerRec customer) {

		ImChatRec imChat =
			customer.getImChat ();

		Integer requiredBalance =
			customer.getBalance () < imChat.getMessageCost ()
				? imChat.getMessageCost () - customer.getBalance ()
				: 0;

		return new ImChatCustomerData ()

			.code (
				underscoreToHyphen (
					customer.getCode ()))

			.email (
				customer.getEmail ())

			.conditionsAccepted (
				customer.getAcceptedTermsAndConditions ())

			.detailsCompleted (
				customer.getDetailsCompleted ())

			.balance (
				customer.getBalance ())

			.balanceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) customer.getBalance ()))

			.minimumBalance (
				imChat.getMessageCost ())

			.minimumBalanceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) imChat.getMessageCost ()))

			.requiredBalance (
				requiredBalance)

			.requiredBalanceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) requiredBalance))

			.details (
				customerDetailData (
					customer));

	}

	@Override
	public
	List<ImChatCustomerDetailData> customerDetailData (
			@NonNull ImChatCustomerRec customer) {

		ImChatRec imChat =
			customer.getImChat ();

		ImmutableList.Builder<ImChatCustomerDetailData> returnBuilder =
			ImmutableList.<ImChatCustomerDetailData>builder ();

		for (
			ImChatCustomerDetailTypeRec customerDetailType
				: imChat.getCustomerDetailTypes ()
		) {

			ImChatCustomerDetailValueRec customerDetailValue =
				customer.getDetails ().get (
					customerDetailType.getId ());

			returnBuilder.add (
				new ImChatCustomerDetailData ()

				.code (
					underscoreToHyphen (
						customerDetailType.getCode ()))

				.label (
					customerDetailType.getLabel ())

				.help (
					customerDetailType.getHelp ())

				.required (
					customerDetailType.getRequired ())

				.dataType (
					camelToHyphen (
						customerDetailType.getDataType ().toString ()))

				.minimumAge (
					customerDetailType.getMinimumAge ())

				.value (
					customerDetailValue != null
						? customerDetailValue.getValue ()
						: null)

			);

		}

		return returnBuilder.build ();

	}

	@Override
	public
	ImChatConversationData conversationData (
			@NonNull ImChatConversationRec conversation) {

		return new ImChatConversationData ()

			.index (
				conversation.getIndex ())

			.profile (
				profileData (
					conversation.getImChatProfile ()))

			.replyPending (
				conversation.getPendingReply ());

	}

	@Override
	public
	ImChatMessageData messageData (
			@NonNull ImChatMessageRec message) {

		ImChatConversationRec conversation =
			message.getImChatConversation ();

		ImChatCustomerRec customer =
			conversation.getImChatCustomer ();

		ImChatRec imChat =
			customer.getImChat ();

		return new ImChatMessageData ()

			.index (
				message.getIndex ())

			.sender (
				message.getSenderUser () != null
					? "operator"
					: "customer")

			.messageText (
				message.getMessageText ())

			.timestamp (
				message.getTimestamp ().getMillis ())

			.charge (
				ifNull (
					message.getPrice (),
					0))

			.chargeString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) ifNull (
						message.getPrice (),
						0)));

	}

	@Override
	public
	ImChatPurchaseData purchaseData (
			@NonNull ImChatPurchaseRec purchase) {

		ImChatCustomerRec customer =
			purchase.getImChatCustomer ();

		ImChatRec imChat =
			customer.getImChat ();

		return new ImChatPurchaseData ()

			.token (
				purchase.getToken ())

			.priceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) purchase.getPrice ()))

			.valueString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) purchase.getValue ()));

	}

	@Override
	public
	ImChatPurchaseHistoryData purchaseHistoryData (
			@NonNull ImChatPurchaseRec purchase) {

		ImChatCustomerRec customer =
			purchase.getImChatCustomer ();

		ImChatRec imChat =
			customer.getImChat ();

		return new ImChatPurchaseHistoryData ()

			.priceString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) purchase.getPrice ()))

			.valueString (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) purchase.getValue ()))

			.timestampString (
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					purchase.getCreatedTime ()));

	}

}
