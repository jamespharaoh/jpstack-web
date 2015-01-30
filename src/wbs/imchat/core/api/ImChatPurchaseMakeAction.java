package wbs.imchat.core.api;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatPricePointObjectHelper;
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatPurchaseObjectHelper;
import wbs.imchat.core.model.ImChatPurchaseRec;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;

@PrototypeComponent ("imChatPurchaseMakeAction")
public
class ImChatPurchaseMakeAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

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

		ImChatPurchaseMakeRequest purchaseRequest =
			dataFromJson.fromJson (
				ImChatPurchaseMakeRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// lookup session and customer

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				purchaseRequest.sessionSecret ());

		if (
			session == null
			|| ! session.getActive ()
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"session-invalid")

				.message (
					"The session secret is invalid or the session is no " +
					"longer active");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		// lookup price point

		ImChatPricePointRec pricePoint =
			imChatPricePointHelper.find (
				(int) (long)
				purchaseRequest.pricePointId ());

		if (
			pricePoint == null
			|| pricePoint.getImChat () != imChat
			|| pricePoint.getDeleted ()
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"price-point-invalid")

				.message (
					"The price point id is invalid");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// create purchase

		imChatPurchaseHelper.insert (
			new ImChatPurchaseRec ()

			.setImChatCustomer (
				customer)

			.setIndex (
				customer.getNumPurchases ())

			.setImChatPricePoint (
				pricePoint)

			.setPrice (
				pricePoint.getPrice ())

			.setValue (
				pricePoint.getValue ())

			.setOldBalance (
				customer.getBalance ())

			.setNewBalance (
				+ customer.getBalance ()
				+ pricePoint.getValue ())

			.setTimestamp (
				transaction.now ())

		);

		// update customer

		customer

			.setNumPurchases (
				customer.getNumPurchases () + 1)

			.setBalance (
				+ customer.getBalance ()
				+ pricePoint.getValue ());

		// create response

		ImChatCustomerCreateSuccess successResponse =
			new ImChatCustomerCreateSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
