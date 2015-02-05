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

@PrototypeComponent ("imChatChangePasswordAction")
public 
class ImChatChangePasswordAction
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

			ImChatChangePasswordRequest changePasswordRequest =
				dataFromJson.fromJson (
					ImChatChangePasswordRequest.class,
					jsonValue);

			// begin transaction

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();
		
			// lookup session

			ImChatSessionRec session =
					imChatSessionHelper.findBySecret (
						changePasswordRequest.sessionSecret ());

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

			// check current password
			
			ImChatCustomerRec imChatcustomer =
					session.getImChatCustomer();
			
			if (!changePasswordRequest.currentPassword.equals(imChatcustomer.getPassword())) {
				
				ImChatFailure failureResponse =
						new ImChatFailure ()

						.reason (
							"incorrect-password")

						.message (
							"The specified password is incorrect.");

					return jsonResponderProvider.get ()
						.value (failureResponse);				
				
			}
			
			// update customer password

			imChatCustomerHelper.insert (
				imChatcustomer

				.setPassword (
						changePasswordRequest.newPassword)

			);
			
			// create response

			ImChatForgotPasswordSuccess successResponse =
				new ImChatForgotPasswordSuccess ();

			// commit and return

			transaction.commit ();

			return jsonResponderProvider.get ()
				.value (successResponse);

		}
}
