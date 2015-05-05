package wbs.applications.imchat.api;

import javax.inject.Inject;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.currency.logic.CurrencyLogic;

@SingletonComponent ("imChatApiLogic")
public
class ImChatApiLogicImplementation
	implements ImChatApiLogic {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

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
			ImChatProfileRec profile) {

		return new ImChatProfileData ()

			.code (
				profile.getCode ())

			.name (
				profile.getPublicName ())

			.description (
				profile.getPublicDescription ())

			.imageLink (
				"TODO link");

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
	
	@Override
	public
	ImChatMessageTemplateData messageTemplateData (
			String key, String value) {
		
		return new ImChatMessageTemplateData ()

			.key (
				key)

			.value (
				value);

	}

}
