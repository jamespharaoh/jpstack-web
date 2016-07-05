package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;

@PrototypeComponent ("imChatConditionsAcceptAction")
public
class ImChatConditionsAcceptAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatConditionsAcceptRequest conditionsAcceptRequest =
			dataFromJson.fromJson (
				ImChatConditionsAcceptRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ImChatRec imChat =
			imChatHelper.findOrNull (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"imChatId")));

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				conditionsAcceptRequest.sessionSecret ());

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		if (

			isNull (
				session)

			|| ! session.getActive ()

			|| notEqual (
				customer.getImChat (),
				imChat)

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

		if (! customer.getAcceptedTermsAndConditions ()) {

			// accept terms and conditions

			customer

				.setAcceptedTermsAndConditions (
					true);

			eventLogic.createEvent (
				"im_chat_customer_conditions_accepted",
				customer);

		}

		// create response

		ImChatConditionsAcceptSuccess successResponse =
			new ImChatConditionsAcceptSuccess ()

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
