package wbs.imchat.core.api;

import static wbs.framework.utils.etc.Misc.equalIgnoreCase;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.eBLBaseComponents.ErrorType;
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
import wbs.paypal.logic.PaypalLogic;
import wbs.paypal.model.PaypalAccountRec;
import wbs.paypal.model.PaypalPaymentObjectHelper;
import wbs.paypal.model.PaypalPaymentRec;
import wbs.paypal.model.PaypalPaymentState;

import com.google.common.base.Optional;

@Log4j
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
	PaypalLogic paypalApiLogic;

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

		ImChatPurchaseGetConfirmationRequest purchaseRequest =
			dataFromJson.fromJson (
				ImChatPurchaseGetConfirmationRequest.class,
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
			session.getImChatCustomer();

		// lookup paypal payment

		PaypalPaymentRec paypalPayment =
			paypalPaymentHelper.findByToken (
				purchaseRequest.purchaseToken ());

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
			paypalApiLogic.expressCheckoutProperties (
				paypalAccount);

		Optional<String> payerId =
			getExpressCheckout (
				purchaseRequest.paypalToken (),
				expressCheckoutProperties);

		// update payment status

		paypalPayment

			.setState (
				PaypalPaymentState.pending);

		// create response

		ImChatPurchaseGetConfirmationSuccess successResponse =
			new ImChatPurchaseGetConfirmationSuccess ()

			.purchaseToken(
				purchaseRequest.purchaseToken ())

			.paypalToken (
				purchaseRequest.paypalToken ())

			.payerId (
				payerId.get ())

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	@SneakyThrows (Exception.class)
	public
	Optional<String> getExpressCheckout (
			String paypalToken,
			Map<String,String> expressCheckoutProperties) {

		// GetExpressCheckoutDetailsReq

		GetExpressCheckoutDetailsReq detailsRequest =
			new GetExpressCheckoutDetailsReq ();

		GetExpressCheckoutDetailsRequestType detailsRequestType =
			new GetExpressCheckoutDetailsRequestType (
				paypalToken);

		detailsRequest.setGetExpressCheckoutDetailsRequest (
			detailsRequestType);

		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service =
			new PayPalAPIInterfaceServiceService (
				expressCheckoutProperties);

		GetExpressCheckoutDetailsResponseType responseType =
			service.getExpressCheckoutDetails (
				detailsRequest);

		// Accessing response parameters

		if (
			equalIgnoreCase (
				responseType.getAck ().getValue (),
				"success")
		) {

			return Optional.of (
				responseType.getGetExpressCheckoutDetailsResponseDetails ()
					.getPayerInfo ()
					.getPayerID ());

		} else {

			for (
				ErrorType error
					: responseType.getErrors ()
			) {

				log.error (
					stringFormat (
						"Paypal error: %s",
						error.getLongMessage ()));

			}

			return Optional.absent ();

		}

	}

}
