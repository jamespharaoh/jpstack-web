package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

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
import wbs.framework.application.config.WbsConfig;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.EmailLogic;
import wbs.framework.utils.RandomLogic;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatForgotPasswordAction")
public
class ImChatForgotPasswordAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	EmailLogic emailLogic;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	RandomLogic randomLogic;

	@Inject
	WbsConfig wbsConfig;

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
			database.beginReadWrite (
				this);

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
					"email-invalid")

				.message (
					"There is no customer with the email address specified");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// generate new password

		String newPassword =
			randomLogic.generateLowercase (12);

		// update customer password

		imChatcustomer

			.setPassword (
				newPassword);

		// send new password via mail

		emailLogic.sendEmail (
			wbsConfig.defaultEmailAddress (),
			forgotPasswordRequest.email (),
			"Chat-app new password",
			stringFormat (
				"Please log on with your new password:\n",
				"\n",
				"%s\n",
				newPassword));

		// create response

		ImChatForgotPasswordSuccess successResponse =
			new ImChatForgotPasswordSuccess ();

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
