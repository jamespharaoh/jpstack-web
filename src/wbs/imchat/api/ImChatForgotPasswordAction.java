package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;

import lombok.Cleanup;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.logic.ImChatLogic;

@PrototypeComponent ("imChatForgotPasswordAction")
public
class ImChatForgotPasswordAction
	implements Action {

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

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	WbsConfig wbsConfig;

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

		ImChatForgotPasswordRequest forgotPasswordRequest =
			dataFromJson.fromJson (
				ImChatForgotPasswordRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatForgotPasswordAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				parseIntegerRequired (
					requestContext.requestStringRequired (
						"imChatId")));

		// check for existing

		ImChatCustomerRec imChatCustomer =
			imChatCustomerHelper.findByEmail (
				imChat,
				forgotPasswordRequest.email ());

		if (imChatCustomer == null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-invalid")

				.message (
					"There is no customer with the email address specified");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// generate new password

		imChatLogic.customerPasswordGenerate (
			imChatCustomer,
			Optional.absent ());

		// create response

		ImChatForgotPasswordSuccess successResponse =
			new ImChatForgotPasswordSuccess ();

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
