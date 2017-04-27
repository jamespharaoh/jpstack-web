package wbs.imchat.api;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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

import wbs.utils.random.RandomLogic;

import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatPricePointObjectHelper;
import wbs.imchat.model.ImChatPurchaseObjectHelper;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatPurchaseGetConfirmationAction")
public
class ImChatPurchaseGetConfirmationAction
	implements Action {

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
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

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

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ImChatPurchaseGetConfirmationAction.handle ()",
					this);

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

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

			Optional <String> payerId;

			try {

				payerId =
					paypalApi.getExpressCheckout (
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

}
