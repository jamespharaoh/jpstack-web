package wbs.imchat.core.api;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatRec;

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

		String newPassword = generateRandomString();

		// update customer password

		imChatCustomerHelper.insert (
			imChatcustomer

			.setPassword (
				newPassword)

		);

		// send new password via mail

        String to = forgotPasswordRequest.email ();
        String from = "services@wellbehavedsoftware.com";
        String host = "wellbehavedsoftware.com";
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);
		try{
		     MimeMessage message = new MimeMessage(session);
		     message.setFrom(new InternetAddress(from));
		     message.addRecipient(Message.RecipientType.TO,
		                              new InternetAddress(to));
		     message.setSubject("Chat-app new password");
		     message.setText("Your new password for chat-app is: " + newPassword + ".");
		     Transport.send(message);
		}catch (MessagingException mex) {
		     mex.printStackTrace();
		}

		// create response

		ImChatForgotPasswordSuccess successResponse =
			new ImChatForgotPasswordSuccess ();

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	protected String generateRandomString() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder pass = new StringBuilder();
        while (pass.length() < 18) {
            int index = (int) (random.nextFloat() * chars.length());
            pass.append(chars.charAt(index));
        }
        String password = pass.toString();
        return password;

    }

}
