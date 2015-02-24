package wbs.imchat.core.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.Map;

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
import wbs.imchat.core.model.ImChatPurchaseObjectHelper;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;
import wbs.paypal.logic.PaypalApi;
import wbs.paypal.logic.PaypalLogic;
import wbs.paypal.model.PaypalAccountRec;
import wbs.paypal.model.PaypalPaymentObjectHelper;
import wbs.paypal.model.PaypalPaymentRec;
import wbs.paypal.model.PaypalPaymentState;

@PrototypeComponent ("imChatPurchaseConfirmAction")
public
class ImChatPurchaseConfirmAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	PaypalApi paypalApi;

	@Inject
	PaypalLogic paypalLogic;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	PaypalPaymentObjectHelper paypalPaymentHelper;

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

		ImChatPurchaseConfirmRequest purchaseRequest =
			dataFromJson.fromJson (
					ImChatPurchaseConfirmRequest.class,
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

		// lookup paypal payment

		PaypalPaymentRec paypalPayment =
			paypalPaymentHelper.findByToken (
				purchaseRequest.purchaseToken ());

		if (paypalPayment == null)
			throw new RuntimeException ();

		if (paypalPayment.getState () != PaypalPaymentState.pending) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"payment-invalid")

				.message (
					stringFormat (
						"The payment is no longer pending"));

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// confirm payment and obtain payerId

		PaypalAccountRec paypalAccount =
			imChat.getPaypalAccount();

		Map<String,String> expressCheckoutProperties =
			paypalLogic.expressCheckoutProperties (
				paypalAccount);

		Boolean checkoutSuccess =
			paypalApi.doExpressCheckout (
				purchaseRequest.paypalToken (),
				purchaseRequest.payerId (),
				paypalPayment.getValue ().toString () + ".0",
				expressCheckoutProperties);

		if (! checkoutSuccess) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"payment-invalid")

				.message (
					"The payment failed");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// update payment status

		paypalPayment

			.setState (
				PaypalPaymentState.confirmed);

		// update customer

		customer

			.setNumPurchases (
				customer.getNumPurchases () + 1)

			.setBalance (
				+ customer.getBalance ()
				+ paypalPayment.getValue ());

		// create response

		ImChatPurchaseConfirmSuccess successResponse =
			new ImChatPurchaseConfirmSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
