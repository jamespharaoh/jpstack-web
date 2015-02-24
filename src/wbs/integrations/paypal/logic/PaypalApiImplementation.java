package wbs.integrations.paypal.logic;

import static wbs.framework.utils.etc.Misc.equalIgnoreCase;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.ErrorType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;
import wbs.framework.application.annotations.SingletonComponent;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("paypalApi")
public
class PaypalApiImplementation
	implements PaypalApi {

	@Override
	@SneakyThrows (Exception.class)
	public
	Optional<String> setExpressCheckout (
			@NonNull String amount,
			@NonNull String returnUrl,
			@NonNull String cancelUrl,
			@NonNull Map<String,String> expressCheckoutProperties) {

		SetExpressCheckoutRequestDetailsType requestDetails =
			new SetExpressCheckoutRequestDetailsType ();

		requestDetails.setReturnURL (
			returnUrl);

		requestDetails.setCancelURL (
			cancelUrl);

		// Payment Information

		List<PaymentDetailsType> paymentDetailsList =
			new ArrayList<PaymentDetailsType> ();

		PaymentDetailsType paymentDetails =
			new PaymentDetailsType ();

		BasicAmountType orderTotal =
			new BasicAmountType (
				CurrencyCodeType.GBP,
				amount);

		paymentDetails.setOrderTotal (
			orderTotal);

		paymentDetails.setPaymentAction (
			PaymentActionCodeType.SALE);

		// Your URL for receiving Instant Payment Notification (IPN)

		paymentDetailsList.add (
			paymentDetails);

		requestDetails.setPaymentDetails (
			paymentDetailsList);

		// Express checkout

		SetExpressCheckoutReq checkoutReq =
			new SetExpressCheckoutReq ();

		SetExpressCheckoutRequestType checkoutRequest =
			new SetExpressCheckoutRequestType (
				requestDetails);

		checkoutReq.setSetExpressCheckoutRequest (
			checkoutRequest);

		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service =
			new PayPalAPIInterfaceServiceService (
				expressCheckoutProperties);

		SetExpressCheckoutResponseType response =
			service.setExpressCheckout (
				checkoutReq);

		// Accessing response parameters

		if (
			equalIgnoreCase (
				response.getAck ().getValue (),
				"success")
		) {

			return Optional.of (
				stringFormat (
					"https://www.sandbox.paypal.com/cgi-bin/webscr",
					"?cmd=_express-checkout",
					"&token=%u",
					response.getToken ()));

		} else {

			for (
				ErrorType error
					: response.getErrors ()
			) {

				log.error (
					stringFormat (
						"Paypal error: %s",
						error.getLongMessage ()));

			}

			return Optional.absent ();

		}

	}

	@Override
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

	@Override
	@SneakyThrows (Exception.class)
	public
	Boolean doExpressCheckout (
			String paypalToken,
			String payerId,
			String amount,
			Map<String,String> expressCheckoutProperties) {

		// DoExpressCheckoutPaymentReq

		DoExpressCheckoutPaymentReq doExpressCheckoutPaymentReq =
			new DoExpressCheckoutPaymentReq ();

		DoExpressCheckoutPaymentRequestDetailsType doExpressCheckoutPaymentRequestDetails =
			new DoExpressCheckoutPaymentRequestDetailsType ();

		// Set the token and payerId

		doExpressCheckoutPaymentRequestDetails.setToken (
			paypalToken);

		doExpressCheckoutPaymentRequestDetails.setPayerID (
			payerId);

		// Payment Information

		List<PaymentDetailsType> paymentDetailsList =
			new ArrayList<PaymentDetailsType> ();

		PaymentDetailsType paymentDetails =
			new PaymentDetailsType ();

		BasicAmountType orderTotal =
			new BasicAmountType (
				CurrencyCodeType.GBP,
				amount);

		paymentDetails.setOrderTotal (
			orderTotal);

		paymentDetails.setPaymentAction (
			PaymentActionCodeType.SALE);

		paymentDetailsList.add (
			paymentDetails);

		doExpressCheckoutPaymentRequestDetails.setPaymentDetails (
			paymentDetailsList);

		DoExpressCheckoutPaymentRequestType doExpressCheckoutPaymentRequest =
			new DoExpressCheckoutPaymentRequestType (
			doExpressCheckoutPaymentRequestDetails);

		doExpressCheckoutPaymentReq.setDoExpressCheckoutPaymentRequest (
			doExpressCheckoutPaymentRequest);

		// Creating service wrapper object

		PayPalAPIInterfaceServiceService service =
			new PayPalAPIInterfaceServiceService (
				expressCheckoutProperties);

		DoExpressCheckoutPaymentResponseType doExpressCheckoutPaymentResponse =
			service.doExpressCheckoutPayment (
				doExpressCheckoutPaymentReq);

		// Accessing response parameters

		if (
			equalIgnoreCase (
				doExpressCheckoutPaymentResponse.getAck ().getValue (),
				"success")
		) {

			List<PaymentInfoType> paymentInfoList =
				doExpressCheckoutPaymentResponse
					.getDoExpressCheckoutPaymentResponseDetails ()
					.getPaymentInfo ();

			if (paymentInfoList != null) {

				for (
					PaymentInfoType paymentInfo
						: paymentInfoList
				) {

					log.info (
						stringFormat (
							"Transaction id: %s",
							paymentInfo.getTransactionID ()));

				}

			}

			return true;

		} else {

			for (
				ErrorType error
					: doExpressCheckoutPaymentResponse.getErrors ()
			) {

				log.error (
					stringFormat (
						"Paypal error: %s",
						error.getLongMessage ()));

			}

			return false;

		}

	}

}
