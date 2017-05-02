package wbs.imchat.api;

import java.util.List;
import java.util.Map;

import wbs.framework.database.Transaction;

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
			Transaction parentTransaction,
			ImChatPricePointRec pricePoint);

	ImChatProfileData profileData (
			Transaction parentTransaction,
			ImChatProfileRec profile);

	ImChatCustomerData customerData (
			Transaction parentTransaction,
			ImChatCustomerRec customer);

	List <ImChatCustomerDetailData> customerDetailData (
			Transaction parentTransaction,
			ImChatCustomerRec customer);

	List <ImChatCustomerDetailData> createDetailData (
			Transaction parentTransaction,
			ImChatRec imChat);

	ImChatConversationData conversationData (
			Transaction parentTransaction,
			ImChatConversationRec conversation);

	ImChatMessageData messageData (
			Transaction parentTransaction,
			ImChatMessageRec message);

	ImChatPurchaseData purchaseData (
			Transaction parentTransaction,
			ImChatPurchaseRec purchase);

	ImChatPurchaseHistoryData purchaseHistoryData (
			Transaction parentTransaction,
			ImChatPurchaseRec purchase);

	Map <String, String> updateCustomerDetails (
			Transaction parentTransaction,
			ImChatCustomerRec customer,
			Map <String, String> newDetails);

}
