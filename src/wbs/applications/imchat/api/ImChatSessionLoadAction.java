package wbs.applications.imchat.api;

import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatSessionLoadAction")
public
class ImChatSessionLoadAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

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

		ImChatSessionLoadRequest sessionLoadRequest =
			dataFromJson.fromJson (
				ImChatSessionLoadRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatSessionLoadAction.handle ()",
				this);

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				sessionLoadRequest.sessionSecret ());

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

		// get customer and conversation

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		// create response

		ImChatSessionLoadSuccess successResponse =
			new ImChatSessionLoadSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer))

			.conversation (
				customer.getCurrentConversation () != null
					? imChatApiLogic.conversationData (
						customer.getCurrentConversation ())
					: null);

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
