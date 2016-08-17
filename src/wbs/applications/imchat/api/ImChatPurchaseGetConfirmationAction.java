package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;

import lombok.Cleanup;
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
import wbs.framework.utils.RandomLogic;
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

@PrototypeComponent ("imChatPurchaseGetConfirmationAction")
public
class ImChatPurchaseGetConfirmationAction
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
	RandomLogic randomLogic;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

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

		ImChatPurchaseGetConfirmationRequest confirmationRequest =
			dataFromJson.fromJson (
				ImChatPurchaseGetConfirmationRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatPurchaseGetConfirmationAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				requestContext.requestIntegerRequired (
					"imChatId"));

		// lookup purchase

		ImChatPurchaseRec purchase =
			imChatPurchaseHelper.findByToken (
				confirmationRequest.token ());

		if (purchase == null)
			throw new RuntimeException ();

		ImChatCustomerRec customer =
			purchase.getImChatCustomer ();

		// lookup session

		ImChatSessionRec session =
			customer.getActiveSession ();

		if (
			isNull (
				session)
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"session-invalid")

				.message (
					"The session secret is invalid or the session is no " +
					"longer active");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// lookup paypal payment

		PaypalPaymentRec paypalPayment =
			purchase.getPaypalPayment ();

		if (paypalPayment == null)
			throw new RuntimeException ();

		if (paypalPayment.getState () != PaypalPaymentState.started) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"payment-invalid")

				.message (
					"The payment is not in the right state");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// confirm payment and obtain payerId

		PaypalAccountRec paypalAccount =
			imChat.getPaypalAccount ();

		Map<String,String> expressCheckoutProperties =
			paypalLogic.expressCheckoutProperties (
				paypalAccount);

		Optional<String> payerId =
			paypalApi.getExpressCheckout (
				confirmationRequest.paypalToken (),
				expressCheckoutProperties);

		// update payment status

		paypalPayment

			.setPaypalToken (
				confirmationRequest.paypalToken ())

			.setPaypalPayerId (
				payerId.get ())

			.setState (
				PaypalPaymentState.pending);

		// create response

		ImChatPurchaseGetConfirmationSuccess successResponse =
			new ImChatPurchaseGetConfirmationSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				imChatApiLogic.customerData (
					customer))

			.purchase (
				imChatApiLogic.purchaseData (
					purchase));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
