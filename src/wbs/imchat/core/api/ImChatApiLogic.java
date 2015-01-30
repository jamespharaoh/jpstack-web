package wbs.imchat.core.api;

import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatProfileRec;

public
interface ImChatApiLogic {

	ImChatPricePointData pricePointData (
			ImChatPricePointRec pricePoint);

	ImChatProfileData profileData (
			ImChatProfileRec profile);

	ImChatCustomerData customerData (
			ImChatCustomerRec customer);

	ImChatConversationData conversationData (
			ImChatConversationRec conversation);

}
