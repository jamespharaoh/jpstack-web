package wbs.imchat.core.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatPurchaseObjectHelper;
import wbs.imchat.core.model.ImChatPurchaseRec;

@PrototypeComponent ("imChatPurchaseHistoryAction")
public 
class ImChatPurchaseHistoryAction
	implements Action {

// dependencies

@Inject
Database database;

@Inject
ImChatCustomerObjectHelper imChatCustomerHelper;

@Inject
ImChatPurchaseObjectHelper imChatPurchaseHelper;

@Inject
RequestContext requestContext;

// prototype dependencies

@Inject
Provider<JsonResponder> jsonResponderProvider;

// implementation

@Override
@SneakyThrows (IOException.class)
public
Responder handle () {
	
	DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatPurchaseHistoryRequest purchaseHistoryRequest =
			dataFromJson.fromJson (
					ImChatPurchaseHistoryRequest.class,
				jsonValue);

	// begin transaction

	@Cleanup
	Transaction transaction =
		database.beginReadOnly ();
	
	// find customer

	ImChatCustomerRec customer =
			imChatCustomerHelper.find(
					purchaseHistoryRequest.customerId());

	if (customer == null) {

		ImChatFailure failureResponse =
			new ImChatFailure ()

			.reason (
				"customer-invalid")

			.message (
				"The customer id is invalid or the customer does " +
				"not exist.");

		return jsonResponderProvider.get ()
			.value (failureResponse);

	}

	// retrieve purchases

	List<ImChatPurchaseRec> purchases =
		new ArrayList<ImChatPurchaseRec> (
				customer.getImChatPurchases());

	Lists.reverse (
		purchases);
	
	// create response

	ImChatPurchaseHistorySuccess purchaseHistoryResponse
		= new ImChatPurchaseHistorySuccess();

	purchaseHistoryResponse.balance = customer.getBalance();
	
	for (
			ImChatPurchaseRec purchase
			: purchases
	) {

		purchaseHistoryResponse.purchases.add (
			new ImChatPurchaseData ()

			.price (
				purchase.getPrice ())

			.value (
				purchase.getValue ())

			.timestamp (
				purchase.getTimestamp().toString())
		);

	}
	
	return jsonResponderProvider.get ()
		.value (purchaseHistoryResponse);

}

}