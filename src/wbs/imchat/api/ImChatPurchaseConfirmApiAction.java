package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.uppercase;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.paypal.logic.PaypalApi;
import wbs.integrations.paypal.logic.PaypalLogic;
import wbs.integrations.paypal.model.PaypalAccountRec;
import wbs.integrations.paypal.model.PaypalPaymentObjectHelper;
import wbs.integrations.paypal.model.PaypalPaymentRec;
import wbs.integrations.paypal.model.PaypalPaymentState;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatPricePointObjectHelper;
import wbs.imchat.model.ImChatPurchaseObjectHelper;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatPurchaseConfirmApiAction")
public
class ImChatPurchaseConfirmApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@SingletonDependency
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PaypalApi paypalApi;

	@SingletonDependency
	PaypalLogic paypalLogic;

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
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			JSONObject jsonValue =
				(JSONObject)
				JSONValue.parse (
					requestContext.requestBodyString ());

			ImChatPurchaseConfirmRequest purchaseRequest =
				dataFromJson.fromJson (
					ImChatPurchaseConfirmRequest.class,
					jsonValue);

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup session and customer

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					purchaseRequest.sessionSecret ());

			if (
				session == null
				|| ! session.getActive ()
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));


			}

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			// lookup purchase

			ImChatPurchaseRec purchase =
				imChatPurchaseHelper.findByToken (
					transaction,
					purchaseRequest.purchaseToken ());

			if (
				purchase == null
				|| purchase.getImChatCustomer () != customer
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"purchase-invalid",
						"The purchase id is not valid"));

			}

			// lookup paypal payment

			PaypalPaymentRec paypalPayment =
				purchase.getPaypalPayment ();

			if (paypalPayment == null)
				throw new RuntimeException ();

			if (paypalPayment.getState () != PaypalPaymentState.pending) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"purchase-invalid",
						"The purchase id is not valid"));

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
						transaction,
						paypalPayment.getPaypalToken (),
						paypalPayment.getPaypalPayerId (),
						uppercase (
							imChat.getBillingCurrency ().getCode ()),
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

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"payment-invalid",
						"The payment failed"));

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
						transaction,
						customer));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.get ()

				.value (
					successResponse)

			);

		}

	}

}
