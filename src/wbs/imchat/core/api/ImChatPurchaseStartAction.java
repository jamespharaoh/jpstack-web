package wbs.imchat.core.api;

import java.io.IOException;
import java.util.Map;

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
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatPurchaseObjectHelper;
import wbs.imchat.core.model.ImChatPurchaseRec;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;
import wbs.integrations.paypal.logic.PaypalApi;
import wbs.integrations.paypal.logic.PaypalLogic;
import wbs.integrations.paypal.model.PaypalAccountRec;
import wbs.integrations.paypal.model.PaypalPaymentObjectHelper;
import wbs.integrations.paypal.model.PaypalPaymentRec;
import wbs.integrations.paypal.model.PaypalPaymentState;
import wbs.platform.currency.logic.CurrencyLogic;

import com.google.common.base.Optional;

@PrototypeComponent ("imChatPurchaseStartAction")
public
class ImChatPurchaseStartAction
	implements Action {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

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

		ImChatPurchaseStartRequest purchaseRequest =
			dataFromJson.fromJson (
				ImChatPurchaseStartRequest.class,
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
			imChat.getPaypalAccount ();

		// create paypal payment

		PaypalPaymentRec paypalPayment =
			paypalPaymentHelper.insert (
				new PaypalPaymentRec ()

			.setPaypalAccount (
				paypalAccount)

			.setValue (
				pricePoint.getValue ())

			.setState (
				PaypalPaymentState.started)

		);

		// create purchase

		ImChatPurchaseRec purchase =
			imChatPurchaseHelper.insert (
				new ImChatPurchaseRec ()

			.setImChatCustomer (
				customer)

			.setIndex (
				customer.getNumPurchases ())

			.setToken (
				imChatPurchaseHelper.generateToken ())

			.setImChatPricePoint (
				pricePoint)

			.setPrice (
				pricePoint.getPrice ())

			.setValue (
				pricePoint.getValue ())

			.setCreatedTime (
				transaction.now ())

			.setPaypalPayment (
				paypalPayment)

		);

		// update customer

		customer

			.setNumPurchases (
				customer.getNumPurchases () + 1);

		// update paypal

		Map<String,String> expressCheckoutProperties =
			paypalLogic.expressCheckoutProperties (
				paypalAccount);

		Optional<String> redirectUrl =
			paypalApi.setExpressCheckout (
				currencyLogic.formatSimple (
					imChat.getCurrency (),
					(long) pricePoint.getValue ()),
				purchaseRequest.successUrl ().replace (
					"{token}",
					purchase.getToken ()),
				purchaseRequest.failureUrl ().replace (
					"{token}",
					purchase.getToken ()),
				expressCheckoutProperties);

		// create response

		ImChatPurchaseStartSuccess successResponse =
			new ImChatPurchaseStartSuccess ()

			.redirectUrl (
				redirectUrl.get ())

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
