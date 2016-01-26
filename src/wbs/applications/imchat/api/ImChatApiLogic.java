package wbs.applications.imchat.api;

import java.util.List;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;

public
interface ImChatApiLogic {

	ImChatPricePointData pricePointData (
			ImChatPricePointRec pricePoint);

	ImChatProfileData profileData (
			ImChatProfileRec profile);

	ImChatCustomerData customerData (
			ImChatCustomerRec customer);

	List<ImChatCustomerDetailData> customerDetailData (
			ImChatCustomerRec customer);

	List<ImChatCustomerDetailData> createDetailData (
			ImChatRec imChat);

	ImChatConversationData conversationData (
			ImChatConversationRec conversation);

	ImChatMessageData messageData (
			ImChatMessageRec message);

	ImChatPurchaseData purchaseData (
			ImChatPurchaseRec purchase);

	ImChatPurchaseHistoryData purchaseHistoryData (
			ImChatPurchaseRec purchase);

}
