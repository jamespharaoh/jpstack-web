package wbs.imchat.core.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.ErrorType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;
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
import wbs.paypal.api.PaypalApiLogic;
import wbs.paypal.model.PaypalAccountRec;
import wbs.paypal.model.PaypalPaymentObjectHelper;
import wbs.paypal.model.PaypalPaymentRec;

@PrototypeComponent ("imChatPurchaseStartAction")
public
class ImChatPurchaseStartAction
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
	PaypalPaymentObjectHelper paypalPaymentHelper;

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

		// get customer

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

		// get paypal account
		
		PaypalAccountRec paypalAccount =
				imChat.getPaypalAccount();
		
		// create paypal payment and purchase
		
		String token = paypalPaymentHelper.generateToken();

		PaypalPaymentRec paypalPayment =
				paypalPaymentHelper.insert (
					new PaypalPaymentRec()

					.setPaypalAccount(
							paypalAccount)

					.setValue(pricePoint.getValue ())

					.setToken(token)

					.setStatus("started")
				);

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

			.setPaypalPayment(paypalPayment)

		);
		
		// create properties needed for the call
		
		Properties expressCheckoutProperties = 
			paypalApiLogic.paypalAccountData(paypalAccount)
				.generateProperties();		

		String redirectURL = setExpressCheckout(pricePoint.getValue ().toString() + ".0", token, expressCheckoutProperties);

		// create response

		ImChatPurchaseStartSuccess successResponse =
			new ImChatPurchaseStartSuccess ()

			.redirectURL (
				redirectURL)

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	//Calls Paypal API and returns an access token

	public String setExpressCheckout(String amount, String token, Properties expressCheckoutProperties) {

		SetExpressCheckoutRequestDetailsType setExpressCheckoutRequestDetails = new SetExpressCheckoutRequestDetailsType();
		setExpressCheckoutRequestDetails.setReturnURL("http://chat.dev.wbsoft.co/test.html#payment=pending&token="+token);
		setExpressCheckoutRequestDetails.setCancelURL("http://chat.dev.wbsoft.co/test.html#payment=error&token="+token);

		// Payment Information

		List<PaymentDetailsType> paymentDetailsList = new ArrayList<PaymentDetailsType>();

		PaymentDetailsType paymentDetails = new PaymentDetailsType();
		BasicAmountType orderTotal = new BasicAmountType(CurrencyCodeType.GBP, amount);
		paymentDetails.setOrderTotal(orderTotal);
		paymentDetails.setPaymentAction(PaymentActionCodeType.SALE);

		// Your URL for receiving Instant Payment Notification (IPN)

		paymentDetails.setNotifyURL("http://chat.dev.wbsoft.co/test.html#payment=finished");
		paymentDetailsList.add(paymentDetails);
		setExpressCheckoutRequestDetails.setPaymentDetails(paymentDetailsList);

		// Express checkout

		SetExpressCheckoutReq setExpressCheckoutReq = new SetExpressCheckoutReq();
		SetExpressCheckoutRequestType setExpressCheckoutRequest = new SetExpressCheckoutRequestType(setExpressCheckoutRequestDetails);
		setExpressCheckoutReq.setSetExpressCheckoutRequest(setExpressCheckoutRequest);

		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service = null;

		service = new PayPalAPIInterfaceServiceService(expressCheckoutProperties);

		SetExpressCheckoutResponseType setExpressCheckoutResponse = null;

		try {
			// Making API call

			setExpressCheckoutResponse = service.setExpressCheckout(setExpressCheckoutReq);

		} catch (Exception e) {

			return "Error api call:" + e;

		}

		// Accessing response parameters

		if (setExpressCheckoutResponse.getAck().getValue().equalsIgnoreCase("success")) {

			return "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=" + setExpressCheckoutResponse.getToken();

		}
		else {

			String errores = "";
			for(Iterator<ErrorType> iter = setExpressCheckoutResponse.getErrors().iterator(); iter.hasNext() ; ) {
				   errores = errores + (iter.next()).getLongMessage() + "   \n";
			}
			return "Error api response \n" + errores;

		}
	}
}
