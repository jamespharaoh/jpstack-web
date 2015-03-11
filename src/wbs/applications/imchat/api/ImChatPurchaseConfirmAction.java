package wbs.applications.imchat.api;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointObjectHelper;
import wbs.applications.imchat.model.ImChatPurchaseObjectHelper;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.paypal.logic.PaypalApi;
import wbs.integrations.paypal.logic.PaypalLogic;
import wbs.integrations.paypal.model.PaypalAccountRec;
import wbs.integrations.paypal.model.PaypalPaymentObjectHelper;
import wbs.integrations.paypal.model.PaypalPaymentRec;
import wbs.integrations.paypal.model.PaypalPaymentState;
import wbs.platform.currency.logic.CurrencyLogic;

@PrototypeComponent ("imChatPurchaseConfirmAction")
public
class ImChatPurchaseConfirmAction
	implements Action {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

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

		// lookup purchase

		ImChatPurchaseRec purchase =
			imChatPurchaseHelper.findByToken (
				purchaseRequest.purchaseToken ());

		if (
			purchase == null
			|| purchase.getImChatCustomer () != customer
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"purchase-invalid")

				.message (
					"The purchase id is not valid");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// lookup paypal payment

		PaypalPaymentRec paypalPayment =
			purchase.getPaypalPayment ();

		if (paypalPayment == null)
			throw new RuntimeException ();

		if (paypalPayment.getState () != PaypalPaymentState.pending) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"purchase-invalid")

				.message (
					"The purchase id is not valid");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// confirm payment and obtain payerId

		PaypalAccountRec paypalAccount =
			imChat.getPaypalAccount ();

		Map<String,String> expressCheckoutProperties =
			paypalLogic.expressCheckoutProperties (
				paypalAccount);

		Boolean checkoutSuccess =
			paypalApi.doExpressCheckout (
				paypalPayment.getPaypalToken (),
				paypalPayment.getPaypalPayerId (),
				currencyLogic.formatSimple (
					imChat.getCurrency (),
					(long) paypalPayment.getValue ()),
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

		// update purchase

		purchase

			.setCompletedTime (
				transaction.now ());

		// update customer

		customer

			.setBalance (
				+ customer.getBalance ()
				+ purchase.getValue ());

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
