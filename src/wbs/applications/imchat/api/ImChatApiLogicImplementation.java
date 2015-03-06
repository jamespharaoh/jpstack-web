package wbs.applications.imchat.api;

import javax.inject.Inject;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileRec;
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

			.id (
				conversation.getId ())

			.profile (
				profileData (
					conversation.getImChatProfile ()));

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

			.sender (
				message.getSenderUser () != null
					? "operator"
					: "customer")

			.message (
				message.getMessageText ())

			.timestamp (
				message.getTimestamp ().getMillis ());

	}

}
