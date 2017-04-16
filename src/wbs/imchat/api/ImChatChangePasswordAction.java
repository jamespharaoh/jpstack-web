package wbs.imchat.api;

import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

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

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ImChatChangePasswordAction.handle ()",
					this);

		) {

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

}
