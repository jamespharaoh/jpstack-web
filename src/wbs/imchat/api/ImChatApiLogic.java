package wbs.imchat.api;

import java.util.List;
import java.util.Map;

import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatPricePointRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;

public
interface ImChatApiLogic {

	ImChatPricePointData pricePointData (
			ImChatPricePointRec pricePoint);

	ImChatProfileData profileData (
			ImChatProfileRec profile);

	ImChatCustomerData customerData (
			ImChatCustomerRec customer);

	List <ImChatCustomerDetailData> customerDetailData (
			ImChatCustomerRec customer);

	List <ImChatCustomerDetailData> createDetailData (
			ImChatRec imChat);

	ImChatConversationData conversationData (
			ImChatConversationRec conversation);

	ImChatMessageData messageData (
			ImChatMessageRec message);

	ImChatPurchaseData purchaseData (
			ImChatPurchaseRec purchase);

	ImChatPurchaseHistoryData purchaseHistoryData (
			ImChatPurchaseRec purchase);

	Map <String, String> updateCustomerDetails (
			TaskLogger parentTaskLogger,
			ImChatCustomerRec customer,
			Map <String, String> newDetails);

}
