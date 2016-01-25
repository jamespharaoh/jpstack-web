package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.hyphenToUnderscore;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;

import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatPurchaseObjectHelper;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.RandomLogic;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.paypal.logic.PaypalApi;
import wbs.integrations.paypal.logic.PaypalLogic;
import wbs.integrations.paypal.model.PaypalAccountRec;
import wbs.integrations.paypal.model.PaypalPaymentObjectHelper;
import wbs.integrations.paypal.model.PaypalPaymentRec;
import wbs.integrations.paypal.model.PaypalPaymentState;
import wbs.platform.currency.logic.CurrencyLogic;

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
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	PaypalApi paypalApi;

	@Inject
	PaypalLogic paypalLogic;

	@Inject
	PaypalPaymentObjectHelper paypalPaymentHelper;

	@Inject
	RandomLogic randomLogic;

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
			database.beginReadWrite (
				this);

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

				.value (
					failureResponse);

		}

		// get customer

		ImChatCustomerRec customer =
				session.getImChatCustomer ();

		// lookup price point

		ImChatPricePointRec pricePoint =
			imChatPricePointHelper.findByCode (
				imChat,
				hyphenToUnderscore (
					purchaseRequest.pricePointCode ()));

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

		PaypalPaymentRec paypalPayment;

		if (imChat.getDevelopmentMode ()) {

			paypalPayment = null;

		} else {

			paypalPayment =
				paypalPaymentHelper.insert (
					paypalPaymentHelper.createInstance ()

				.setPaypalAccount (
					paypalAccount)

				.setTimestamp (
					transaction.now ())

				.setValue (
					customer.getDeveloperMode ()
						? 1
						: pricePoint.getPrice ())

				.setState (
					PaypalPaymentState.started)

			);

		}

		// create purchase

		ImChatPurchaseRec purchase =
			imChatPurchaseHelper.insert (
				imChatPurchaseHelper.createInstance ()

			.setImChatCustomer (
				customer)

			.setIndex (
				(int) (long)
				customer.getNumPurchases ())

			.setToken (
				randomLogic.generateLowercase (20))

			.setImChatPricePoint (
				pricePoint)

			.setPrice (
				customer.getDeveloperMode ()
					? 1
					: pricePoint.getPrice ())

			.setValue (
				pricePoint.getValue ())

			.setCreatedTime (
				transaction.now ())

			.setCompletedTime (
				imChat.getDevelopmentMode ()
					? transaction.now ()
					: null)

			.setPaypalPayment (
				paypalPayment)

		);

		// update customer

		customer

			.setNumPurchases (
				customer.getNumPurchases () + 1)

			.setBalance (
				imChat.getDevelopmentMode ()
					? customer.getBalance ()
						+ pricePoint.getValue ()
					: customer.getBalance ());

		// update paypal

		Optional<String> redirectUrl;

		if (imChat.getDevelopmentMode ()) {

			redirectUrl =
				Optional.of (
					"development-mode");

		} else {

			Map<String,String> expressCheckoutProperties =
				paypalLogic.expressCheckoutProperties (
					paypalAccount);

			redirectUrl =
				paypalApi.setExpressCheckout (
					currencyLogic.formatSimple (
						imChat.getBillingCurrency (),
						purchase.getPrice ()),
					purchaseRequest.successUrl ().replace (
						"{token}",
						purchase.getToken ()),
					purchaseRequest.failureUrl ().replace (
						"{token}",
						purchase.getToken ()),
					paypalAccount.getCheckoutUrl (),
					expressCheckoutProperties);

		}

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

			.value (
				successResponse);

	}

}
