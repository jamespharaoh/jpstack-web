package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NumberUtils.lessThanZero;
import static wbs.framework.utils.etc.NumberUtils.notLessThan;
import static wbs.framework.utils.etc.NumberUtils.parseIntegerRequired;

import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import wbs.applications.imchat.logic.ImChatLogic;
import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
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

@PrototypeComponent ("imChatConversationEndAction")
public
class ImChatConversationEndAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatConversationObjectHelper imChatConversationHelper;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatLogic imChatLogic;

	@SingletonDependency
	ImChatProfileObjectHelper imChatProfileHelper;

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

		ImChatConversationEndRequest endRequest =
			dataFromJson.fromJson (
				ImChatConversationEndRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatConversationEndAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				parseIntegerRequired (
					requestContext.requestStringRequired (
						"imChatId")));

		// lookup session and customer

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				endRequest.sessionSecret ());

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		if (

			isNull (
				session)

			|| ! session.getActive ()

			|| referenceNotEqualWithClass (
				ImChatRec.class,
				imChat,
				customer.getImChat ())

		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"session-invalid")

				.message (
					"The session secret is invalid or the session is no " +
					"longer active");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// get conversation

		if (

			lessThanZero (
				endRequest.conversationIndex ())

			|| notLessThan (
				endRequest.conversationIndex (),
				customer.getNumConversations ())

		) {

			throw new RuntimeException ();

		}

		ImChatConversationRec conversation =
			imChatConversationHelper.findByIndexRequired (
				customer,
				endRequest.conversationIndex ());

		// end conversation

		if (
			referenceEqualWithClass (
				ImChatConversationRec.class,
				conversation,
				customer.getCurrentConversation ())
		) {

			imChatLogic.conversationEnd (
				conversation);

		}

		// create response

		ImChatConversationEndSuccess successResponse =
			new ImChatConversationEndSuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
