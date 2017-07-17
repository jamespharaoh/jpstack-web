package wbs.imchat.api;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
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

import wbs.utils.random.RandomLogic;

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

@PrototypeComponent ("imChatPurchaseGetConfirmationApiAction")
public
class ImChatPurchaseGetConfirmationApiAction
	implements ApiAction {

	// dependencies

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
	RandomLogic randomLogic;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JsonResponder> jsonResponderProvider;

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

			ImChatPurchaseGetConfirmationRequest confirmationRequest =
				dataFromJson.fromJson (
					ImChatPurchaseGetConfirmationRequest.class,
					requestContext.requestBodyString ());

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup purchase

			ImChatPurchaseRec purchase =
				imChatPurchaseHelper.findByToken (
					transaction,
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

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			// lookup paypal payment

			PaypalPaymentRec paypalPayment =
				purchase.getPaypalPayment ();

			if (paypalPayment == null)
				throw new RuntimeException ();

			if (paypalPayment.getState () != PaypalPaymentState.started) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"payment-invalid",
						"The payment is not in the right state"));

			}

			// confirm payment and obtain payerId

			PaypalAccountRec paypalAccount =
				imChat.getPaypalAccount ();

			Map <String, String> expressCheckoutProperties =
				paypalLogic.expressCheckoutProperties (
					paypalAccount);

			Optional <String> payerId;

			try {

				payerId =
					paypalApi.getExpressCheckout (
						transaction,
						confirmationRequest.paypalToken (),
						expressCheckoutProperties);

			} catch (InterruptedException interruptedException) {

				Thread.currentThread ().interrupt ();

				throw new RuntimeException (
					interruptedException);

			}

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
						transaction,
						customer))

				.purchase (
					imChatApiLogic.purchaseData (
						transaction,
						purchase));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					successResponse)

			));

		}

	}

}
