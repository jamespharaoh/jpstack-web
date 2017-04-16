package wbs.imchat.api;

import static wbs.utils.etc.Misc.isNull;

import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.Lists;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatPurchaseObjectHelper;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatPurchaseHistoryAction")
public
class ImChatPurchaseHistoryAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatPurchaseHistoryRequest purchaseHistoryRequest =
			dataFromJson.fromJson (
					ImChatPurchaseHistoryRequest.class,
				jsonValue);

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ImChatPurchaseHistoryAction.handle ()",
					this);

		) {

			// lookup session and customer

			ImChatSessionRec session =
					imChatSessionHelper.findBySecret (
							purchaseHistoryRequest.sessionSecret ());

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

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			// retrieve purchases

			List<ImChatPurchaseRec> purchases =
				Lists.reverse (
					customer.getPurchases ());

			// create response

			ImChatPurchaseHistorySuccess purchaseHistoryResponse =
				new ImChatPurchaseHistorySuccess ()

				.customer (
					imChatApiLogic.customerData (
						customer));

			for (
				ImChatPurchaseRec purchase
					: purchases
			) {

				if (
					isNull (
						purchase.getCompletedTime ())
				) {
					continue;
				}

				purchaseHistoryResponse.purchases.add (
					imChatApiLogic.purchaseHistoryData (
						purchase));

			}

			return jsonResponderProvider.get ()

				.value (
					purchaseHistoryResponse);

		}

	}

}