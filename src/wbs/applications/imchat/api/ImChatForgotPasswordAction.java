package wbs.applications.imchat.api;

import java.io.IOException;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.email.logic.EmailLogic;

@PrototypeComponent ("imChatForgotPasswordAction")
public
class ImChatForgotPasswordAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	Random random;

	@Inject
	EmailLogic email;

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

		ImChatForgotPasswordRequest forgotPasswordRequest =
			dataFromJson.fromJson (
				ImChatForgotPasswordRequest.class,
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

		// check for existing

		ImChatCustomerRec imChatcustomer =
			imChatCustomerHelper.findByEmail (
				imChat,
				forgotPasswordRequest.email ());

		if (imChatcustomer == null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-does-not-exists")

				.message (
					"The specified customer does not exist.");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// generate new password

		String newPassword =
			generatePassword (12);

		// update customer password

		imChatcustomer

			.setPassword (
				newPassword);

		// send new password via mail

		email.smtpHostname("wellbehavedsoftware.com");
		email.fromAddress("services@wellbehavedsoftware.com");
		email.sendEmail(forgotPasswordRequest.email (), "Chat-app new password", "Your new password for chat-app is: " + newPassword + ".");

		// create response

		ImChatForgotPasswordSuccess successResponse =
			new ImChatForgotPasswordSuccess ();

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	String generatePassword (
			int length) {

        String chars = "abcdefghijklmnopqrstuvwxyz";

        StringBuilder stringBuilder =
        	new StringBuilder ();

		for (int i = 0; i < length; i ++) {

            stringBuilder.append (
            	chars.charAt (
            		random.nextInt (
            			chars.length ())));

        }

        return stringBuilder.toString ();

    }

}
