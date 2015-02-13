package wbs.imchat.core.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.ErrorType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
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
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;
import wbs.paypal.model.PaypalPaymentObjectHelper;
import wbs.paypal.model.PaypalPaymentRec;


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
				|| !paypalPayment.getStatus().equals("pending")
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"payment-invalid")

					.message (
						"The payment is invalid or the payment is no " +
						"longer pending (status: "+paypalPayment.getStatus()+").");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

		// confirm payment and obtain payerId

		String checkoutStatus = doExpressCheckout(purchaseRequest.paypalToken (), purchaseRequest.payerId (), paypalPayment.getValue ().toString() + ".0");

		if (
				!checkoutStatus.contains("Success")
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"payment-invalid")

					.message (
						"The payment is invalid or the payment was " +
						"not confirmed." + checkoutStatus);

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}

		// update payment status

		paypalPaymentHelper.insert (
				paypalPayment.setStatus("confirmed")
			);

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

	public String doExpressCheckout(String paypalToken, String payerId, String amount) {

		// DoExpressCheckoutPaymentReq

		DoExpressCheckoutPaymentReq doExpressCheckoutPaymentReq = new DoExpressCheckoutPaymentReq();
		DoExpressCheckoutPaymentRequestDetailsType doExpressCheckoutPaymentRequestDetails = new DoExpressCheckoutPaymentRequestDetailsType();

		// Set the token and payerId

		doExpressCheckoutPaymentRequestDetails.setToken(paypalToken);
		doExpressCheckoutPaymentRequestDetails.setPayerID(payerId);

		// Payment Information

		List<PaymentDetailsType> paymentDetailsList = new ArrayList<PaymentDetailsType>();

		PaymentDetailsType paymentDetails = new PaymentDetailsType();
		BasicAmountType orderTotal = new BasicAmountType(CurrencyCodeType.GBP, amount);
		paymentDetails.setOrderTotal(orderTotal);
		paymentDetails.setPaymentAction(PaymentActionCodeType.SALE);

		// Your URL for receiving Instant Payment Notification (IPN)

		paymentDetails.setNotifyURL("http://chat.dev.wbsoft.co/test.html#payment=finished");
		paymentDetailsList.add(paymentDetails);
		doExpressCheckoutPaymentRequestDetails.setPaymentDetails(paymentDetailsList);

		DoExpressCheckoutPaymentRequestType doExpressCheckoutPaymentRequest = new DoExpressCheckoutPaymentRequestType(doExpressCheckoutPaymentRequestDetails);
		doExpressCheckoutPaymentReq.setDoExpressCheckoutPaymentRequest(doExpressCheckoutPaymentRequest);
		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service = null;
		try {

			service = new PayPalAPIInterfaceServiceService("conf/sdk_conf.properties");

		} catch (IOException e) {

			return "Error Message : " + e.getMessage();

		}

		DoExpressCheckoutPaymentResponseType doExpressCheckoutPaymentResponse = null;
		try {

			// Making API call

			doExpressCheckoutPaymentResponse = service.doExpressCheckoutPayment(doExpressCheckoutPaymentReq);

		} catch (Exception e) {
			return "Error Message : " + e.getMessage();
		}

		// Accessing response parameters

		if (doExpressCheckoutPaymentResponse.getAck().getValue().equalsIgnoreCase("success")) {

			String details = "";

			if (doExpressCheckoutPaymentResponse.getDoExpressCheckoutPaymentResponseDetails().getPaymentInfo() != null) {

				Iterator<PaymentInfoType> paymentInfoIterator = doExpressCheckoutPaymentResponse.getDoExpressCheckoutPaymentResponseDetails().getPaymentInfo().iterator();


				while (paymentInfoIterator.hasNext()) {
					PaymentInfoType paymentInfo = paymentInfoIterator.next();

					details = details + "Transaction ID : "	+ paymentInfo.getTransactionID() + "\n";
				}
			}

			return "Success \n" + details;
		}

		else {

			String errores = "";
			for(Iterator<ErrorType> iter = doExpressCheckoutPaymentResponse.getErrors().iterator(); iter.hasNext() ; ) {
				   errores = errores + (iter.next()).getLongMessage() + "   \n";
			}
			return "Error api response \n" + errores;
		}

	}

}
