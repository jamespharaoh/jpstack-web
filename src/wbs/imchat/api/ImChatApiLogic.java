package wbs.imchat.api;

import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerCreditRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatPricePointRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;
import wbs.web.responder.WebResponder;

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

	ImChatPurchaseHistoryData purchaseHistoryData (
			Transaction parentTransaction,
			ImChatCustomerCreditRec credit);

	Map <String, String> updateCustomerDetails (
			Transaction parentTransaction,
			ImChatCustomerRec customer,
			Map <String, String> newDetails);

	WebResponder failureResponse (
			TaskLogger parentTaskLogger,
			String reason,
			String message);

	default
	WebResponder failureResponseFormat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String reason,
			@NonNull CharSequence ... messageArguments) {

		return failureResponse (
			parentTaskLogger,
			reason,
			stringFormatArray (
				messageArguments));

	}

}
