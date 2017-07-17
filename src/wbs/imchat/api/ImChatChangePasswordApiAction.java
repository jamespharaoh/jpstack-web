package wbs.imchat.api;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatChangePasswordApiAction")
public
class ImChatChangePasswordApiAction
	implements ApiAction {

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
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			ImChatChangePasswordRequest changePasswordRequest =
				dataFromJson.fromJson (
					ImChatChangePasswordRequest.class,
					requestContext.requestBodyString ());

			// lookup session

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					changePasswordRequest.sessionSecret ());

			if (
				session == null
				|| ! session.getActive ()
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			// check current password

			ImChatCustomerRec imChatcustomer =
				session.getImChatCustomer ();

			if (
				stringNotEqualSafe (
					changePasswordRequest.currentPassword (),
					imChatcustomer.getPassword ())
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"incorrect-password",
						"The specified password is incorrect."));

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

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					successResponse)

			));

		}

	}

}
