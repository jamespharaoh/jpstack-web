package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatForgotPasswordApiAction")
public
class ImChatForgotPasswordApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatLogic imChatLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	WbsConfig wbsConfig;

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

			ImChatForgotPasswordRequest forgotPasswordRequest =
				dataFromJson.fromJson (
					ImChatForgotPasswordRequest.class,
					requestContext.requestBodyString ());

			// lookup object

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// check for existing

			ImChatCustomerRec imChatCustomer =
				imChatCustomerHelper.findByEmail (
					transaction,
					imChat,
					forgotPasswordRequest.email ());

			if (imChatCustomer == null) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"email-invalid",
						"There is no customer with the email address ",
						"specified"));

			}

			// generate new password

			imChatLogic.customerPasswordGenerate (
				transaction,
				imChatCustomer,
				optionalAbsent ());

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
