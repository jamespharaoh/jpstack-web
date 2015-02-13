package wbs.imchat.core.api;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatProfileRec;
import wbs.imchat.core.model.ImChatRec;
import wbs.paypal.model.PaypalPaymentRec;
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

			.id (
				pricePoint.getId ())

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

			.id (
				profile.getId ())

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

			.id (
				customer.getId ())

			.code (
				customer.getCode ())

			.balance (
				customer.getBalance ());

	}

	@Override
	public
	ImChatConversationData conversationData (
			ImChatConversationRec conversation) {

		return new ImChatConversationData ()

			.id (
				conversation.getId ());

	}

	@Override
	public
	ImChatMessageData messageData (
			ImChatMessageRec message) {

		return new ImChatMessageData ()

			.id (
				message.getId ())

			.index (
				message.getIndex ())

			.messageText (
				message.getMessageText ());

	}

	@Override
	public
	PaypalPaymentData paypalPaymentData (
			PaypalPaymentRec paypalPayment) {

		return new PaypalPaymentData ()

			.id (
					paypalPayment.getId ())

			.status (
					paypalPayment.getStatus ());

	}

}
