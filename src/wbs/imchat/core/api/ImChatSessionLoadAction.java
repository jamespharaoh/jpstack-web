package wbs.imchat.core.api;

import java.io.IOException;

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
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;

@PrototypeComponent ("imChatSessionLoadAction")
public 
class ImChatSessionLoadAction
implements Action {
	
// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

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

		ImChatSessionLoadRequest sessionLoadRequest =
			dataFromJson.fromJson (
				ImChatSessionLoadRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();
	
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
		
		ImChatCustomerRec imChatCustomer =
				session.getImChatCustomer();
		
		// create response

		ImChatSessionLoadSuccess successResponse =
			new ImChatSessionLoadSuccess ()

			.customer (
				imChatApiLogic.customerData (
					imChatCustomer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}
}
