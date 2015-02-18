package wbs.imchat.core.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

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
import wbs.paypal.api.PaypalApiLogic;
import wbs.paypal.model.PaypalAccountRec;
import wbs.paypal.model.PaypalPaymentObjectHelper;
import wbs.paypal.model.PaypalPaymentRec;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.eBLBaseComponents.ErrorType;

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
	PaypalApiLogic paypalApiLogic;

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

		if (
				paypalPayment == null
				|| !paypalPayment.getStatus().equals("started")
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"payment-invalid")

					.message (
						"The payment is invalid or the payment is no " +
						"longer started (status: "+paypalPayment.getStatus()+").");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

		// confirm payment and obtain payerId
		
		PaypalAccountRec paypalAccount =
				imChat.getPaypalAccount();
			
			Properties expressCheckoutProperties = 
					paypalApiLogic.paypalAccountData(paypalAccount)
						.generateProperties();		

		String payerId = getExpressCheckout(purchaseRequest.paypalToken (), expressCheckoutProperties);

		// update payment status

		paypalPaymentHelper.insert (
				paypalPayment.setStatus("pending")
			);

		// create response

		ImChatPurchaseGetConfirmationSuccess successResponse =
			new ImChatPurchaseGetConfirmationSuccess ()

			.purchaseToken(
					purchaseRequest.purchaseToken ())

			.paypalToken (
					purchaseRequest.paypalToken ())

			.payerId (
				payerId)

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	public String getExpressCheckout(String paypalToken, Properties expressCheckoutProperties) {

		// GetExpressCheckoutDetailsReq

		GetExpressCheckoutDetailsReq getExpressCheckoutDetailsReq = new GetExpressCheckoutDetailsReq();

		GetExpressCheckoutDetailsRequestType getExpressCheckoutDetailsRequest = new GetExpressCheckoutDetailsRequestType(paypalToken);
		getExpressCheckoutDetailsReq.setGetExpressCheckoutDetailsRequest(getExpressCheckoutDetailsRequest);

		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service = null;

		service = new PayPalAPIInterfaceServiceService(expressCheckoutProperties);

		GetExpressCheckoutDetailsResponseType getExpressCheckoutDetailsResponse = null;

		try {
			// Making API call

			getExpressCheckoutDetailsResponse = service.getExpressCheckoutDetails(getExpressCheckoutDetailsReq);

		} catch (Exception e) {

			return "Error Message : " + e.getMessage();

		}
		// Accessing response parameters

		if (getExpressCheckoutDetailsResponse.getAck().getValue().equalsIgnoreCase("success")) {
			return getExpressCheckoutDetailsResponse.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo().getPayerID();
		}
		else {

			String errores = "";
			for(Iterator<ErrorType> iter = getExpressCheckoutDetailsResponse.getErrors().iterator(); iter.hasNext() ; ) {
				   errores = errores + (iter.next()).getLongMessage() + "   \n";
			}
			return "Error api response \n" + errores;

		}

	}
}
