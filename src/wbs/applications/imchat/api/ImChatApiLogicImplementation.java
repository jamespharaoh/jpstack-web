package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
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
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	ImChatPricePointData pricePointData (
			ImChatPricePointRec pricePoint) {

		ImChatRec imChat =
			pricePoint.getImChat ();

		return new ImChatPricePointData ()

			.code (
				pricePoint.getCode ())

			.name (
				pricePoint.getName ())

			.price (
				currencyLogic.formatText (
					imChat.getCurrency (),
					(long) pricePoint.getPrice ()))

			.value (
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

		return new ImChatProfileData ()

			.code (
				profile.getCode ())

			.name (
				profile.getPublicName ())

			.description (
				profile.getPublicDescription ())

			.imageLink (
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/im-chat-media/%u",
					image.getId (),
					"/%u",
					hash,
					"/original.jpg"))

			.imageWidth (
				image.getWidth ())

			.imageHeight (
				image.getHeight ());

	}

	@Override
	public
	ImChatCustomerData customerData (
			ImChatCustomerRec customer) {

		return new ImChatCustomerData ()

			.code (
				customer.getCode ())

			.email (
				customer.getEmail ())

			.balance (
				customer.getBalance ());

	}

	@Override
	public
	ImChatConversationData conversationData (
			ImChatConversationRec conversation) {

		return new ImChatConversationData ()

			.index (
				conversation.getIndex ())

			.profile (
				profileData (
					conversation.getImChatProfile ()));

	}

	@Override
	public
	ImChatMessageData messageData (
			ImChatMessageRec message) {

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
				message.getTimestamp ().getMillis ());

	}

	@Override
	public
	ImChatPurchaseData purchaseData (
			ImChatPurchaseRec purchase) {

		return new ImChatPurchaseData ()

			.token (
				purchase.getToken ())

			.price (
				purchase.getPrice ())

			.value (
				purchase.getValue ());

	}

}
