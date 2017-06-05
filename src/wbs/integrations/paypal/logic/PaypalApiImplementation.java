package wbs.integrations.paypal.logic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.equalIgnoreCase;
import static wbs.utils.string.StringUtils.replaceAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.google.common.base.Optional;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

import lombok.NonNull;

import org.xml.sax.SAXException;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

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

@SingletonComponent ("paypalApi")
public
class PaypalApiImplementation
	implements PaypalApi {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	Optional <String> setExpressCheckout (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String currency,
			@NonNull String amount,
			@NonNull String returnUrl,
			@NonNull String cancelUrl,
			@NonNull String checkoutUrl,
			@NonNull Map <String, String> expressCheckoutProperties)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setExpressCheckout");

		) {

			// setup request

			SetExpressCheckoutRequestDetailsType requestDetails =
				new SetExpressCheckoutRequestDetailsType ();

			requestDetails.setReturnURL (
				returnUrl);

			requestDetails.setCancelURL (
				cancelUrl);

			List<PaymentDetailsType> paymentDetailsList =
				new ArrayList<PaymentDetailsType> ();

			PaymentDetailsType paymentDetails =
				new PaymentDetailsType ();

			BasicAmountType orderTotal =
				new BasicAmountType (
					CurrencyCodeType.valueOf (
						currency),
					amount);

			paymentDetails.setOrderTotal (
				orderTotal);

			paymentDetails.setPaymentAction (
				PaymentActionCodeType.SALE);

			paymentDetailsList.add (
				paymentDetails);

			requestDetails.setPaymentDetails (
				paymentDetailsList);

			SetExpressCheckoutReq checkoutReq =
				new SetExpressCheckoutReq ();

			SetExpressCheckoutRequestType checkoutRequest =
				new SetExpressCheckoutRequestType (
					requestDetails);

			checkoutReq.setSetExpressCheckoutRequest (
				checkoutRequest);

			// make call

			PayPalAPIInterfaceServiceService service =
				new PayPalAPIInterfaceServiceService (
					expressCheckoutProperties);

			SetExpressCheckoutResponseType response;

			try {

				response =
					service.setExpressCheckout (
						checkoutReq);

			} catch (ClientActionRequiredException clientActionRequiredException) {

				throw new RuntimeException (
					clientActionRequiredException);

			} catch (HttpErrorException httpErrorException) {

				throw new RuntimeException (
					httpErrorException);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			} catch (InvalidCredentialException invalidCredentialException) {

				throw new RuntimeException (
					invalidCredentialException);

			} catch (InvalidResponseDataException invalidResponseDataException) {

				throw new RuntimeException (
					invalidResponseDataException);

			} catch (MissingCredentialException missingCredentialException) {

				throw new RuntimeException (
					missingCredentialException);

			} catch (OAuthException oauthException) {

				throw new RuntimeException (
					oauthException);

			} catch (ParserConfigurationException parserConfigurationException) {

				throw new RuntimeException (
					parserConfigurationException);

			} catch (SAXException saxException) {

				throw new RuntimeException (
					saxException);

			} catch (SSLConfigurationException sslConfigurationException) {

				throw new RuntimeException (
					sslConfigurationException);

			}

			// Accessing response parameters

			if (
				equalIgnoreCase (
					response.getAck ().getValue (),
					"success")
			) {

				return optionalOf (
					replaceAll (
						checkoutUrl,
						"{token}",
						response.getToken ()));

			} else {

				for (
					ErrorType error
						: response.getErrors ()
				) {

					taskLogger.errorFormat (
						"Paypal error: %s",
						error.getLongMessage ());

				}

				return optionalAbsent ();

			}

		}

	}

	@Override
	public
	Optional <String> getExpressCheckout (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String paypalToken,
			@NonNull Map <String, String> expressCheckoutProperties)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getExpressCheckout");

		) {

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

			GetExpressCheckoutDetailsResponseType responseType;

			try {

				responseType =
					service.getExpressCheckoutDetails (
						detailsRequest);

			} catch (ClientActionRequiredException clientActionRequiredException) {

				throw new RuntimeException (
					clientActionRequiredException);

			} catch (HttpErrorException httpErrorException) {

				throw new RuntimeException (
					httpErrorException);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			} catch (InvalidCredentialException invalidCredentialException) {

				throw new RuntimeException (
					invalidCredentialException);

			} catch (InvalidResponseDataException invalidResponseDataException) {

				throw new RuntimeException (
					invalidResponseDataException);

			} catch (MissingCredentialException missingCredentialException) {

				throw new RuntimeException (
					missingCredentialException);

			} catch (OAuthException oauthException) {

				throw new RuntimeException (
					oauthException);

			} catch (ParserConfigurationException parserConfigurationException) {

				throw new RuntimeException (
					parserConfigurationException);

			} catch (SAXException saxException) {

				throw new RuntimeException (
					saxException);

			} catch (SSLConfigurationException sslConfigurationException) {

				throw new RuntimeException (
					sslConfigurationException);

			}

			// Accessing response parameters

			if (
				equalIgnoreCase (
					responseType.getAck ().getValue (),
					"success")
			) {

				return optionalOf (
					responseType.getGetExpressCheckoutDetailsResponseDetails ()
						.getPayerInfo ()
						.getPayerID ());

			} else {

				for (
					ErrorType error
						: responseType.getErrors ()
				) {

					taskLogger.errorFormat (
						"Paypal error: %s",
						error.getLongMessage ());

				}

				return Optional.absent ();

			}

		}

	}

	@Override
	public
	Boolean doExpressCheckout (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String paypalToken,
			@NonNull String payerId,
			@NonNull String currency,
			@NonNull String amount,
			@NonNull Map <String, String> expressCheckoutProperties)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doExpressCheckout");

		) {

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

			List <PaymentDetailsType> paymentDetailsList =
				new ArrayList<> ();

			PaymentDetailsType paymentDetails =
				new PaymentDetailsType ();

			BasicAmountType orderTotal =
				new BasicAmountType (
					CurrencyCodeType.valueOf (
						currency),
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

			DoExpressCheckoutPaymentResponseType doExpressCheckoutPaymentResponse;

			try {

				doExpressCheckoutPaymentResponse =
					service.doExpressCheckoutPayment (
						doExpressCheckoutPaymentReq);

			} catch (ClientActionRequiredException clientActionRequiredException) {

				throw new RuntimeException (
					clientActionRequiredException);

			} catch (HttpErrorException httpErrorException) {

				throw new RuntimeException (
					httpErrorException);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			} catch (InvalidCredentialException invalidCredentialException) {

				throw new RuntimeException (
					invalidCredentialException);

			} catch (InvalidResponseDataException invalidResponseDataException) {

				throw new RuntimeException (
					invalidResponseDataException);

			} catch (MissingCredentialException missingCredentialException) {

				throw new RuntimeException (
					missingCredentialException);

			} catch (OAuthException oauthException) {

				throw new RuntimeException (
					oauthException);

			} catch (ParserConfigurationException parserConfigurationException) {

				throw new RuntimeException (
					parserConfigurationException);

			} catch (SAXException saxException) {

				throw new RuntimeException (
					saxException);

			} catch (SSLConfigurationException sslConfigurationException) {

				throw new RuntimeException (
					sslConfigurationException);

			}

			// Accessing response parameters

			if (
				equalIgnoreCase (
					doExpressCheckoutPaymentResponse.getAck ().getValue (),
					"success")
			) {

				List <PaymentInfoType> paymentInfoList =
					doExpressCheckoutPaymentResponse
						.getDoExpressCheckoutPaymentResponseDetails ()
						.getPaymentInfo ();

				if (paymentInfoList != null) {

					for (
						PaymentInfoType paymentInfo
							: paymentInfoList
					) {

						taskLogger.noticeFormat (
							"Transaction id: %s",
							paymentInfo.getTransactionID ());

					}

				}

				return true;

			} else {

				for (
					ErrorType error
						: doExpressCheckoutPaymentResponse.getErrors ()
				) {

					taskLogger.errorFormat (
						"Paypal error: %s",
						error.getLongMessage ());

				}

				return false;

			}

		}

	}

}
