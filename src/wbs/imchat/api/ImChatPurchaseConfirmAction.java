package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Map;

import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatPricePointObjectHelper;
import wbs.imchat.model.ImChatPurchaseObjectHelper;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
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

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	PaypalApi paypalApi;

	@SingletonDependency
	PaypalLogic paypalLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@SingletonDependency
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@SingletonDependency
	PaypalPaymentObjectHelper paypalPaymentHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
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
			database.beginReadWrite (
				"ImChatPurchaseConfirmAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				parseIntegerRequired (
					requestContext.requestStringRequired (
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

		Map <String, String> expressCheckoutProperties =
			paypalLogic.expressCheckoutProperties (
				paypalAccount);

		Boolean checkoutSuccess;

		try {

			checkoutSuccess =
				paypalApi.doExpressCheckout (
					paypalPayment.getPaypalToken (),
					paypalPayment.getPaypalPayerId (),
					currencyLogic.formatSimple (
						imChat.getBillingCurrency (),
						paypalPayment.getValue ()),
					expressCheckoutProperties);

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

			throw new RuntimeException (
				interruptedException);

		}

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
				+ purchase.getValue ())

			.setTotalPurchaseValue (
				+ customer.getTotalPurchaseValue ()
				+ purchase.getValue ())

			.setTotalPurchasePrice (
				+ customer.getTotalPurchasePrice ()
				+ purchase.getPrice ())

			.setLastSession (
				transaction.now ());

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
