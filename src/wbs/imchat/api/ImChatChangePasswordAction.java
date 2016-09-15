package wbs.imchat.api;

import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
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

@PrototypeComponent ("imChatChangePasswordAction")
public
class ImChatChangePasswordAction
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

		ImChatChangePasswordRequest changePasswordRequest =
			dataFromJson.fromJson (
				ImChatChangePasswordRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatChangePasswordAction.handle ()",
				this);

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
			session.getImChatCustomer ();

		if (
			stringNotEqualSafe (
				changePasswordRequest.currentPassword (),
				imChatcustomer.getPassword ())
		) {

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

		imChatcustomer

			.setPassword (
				changePasswordRequest.newPassword);

		// create response

		ImChatForgotPasswordSuccess successResponse =
			new ImChatForgotPasswordSuccess ();

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
