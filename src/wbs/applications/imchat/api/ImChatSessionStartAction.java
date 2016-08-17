package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;
import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.RandomLogic;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.text.model.TextObjectHelper;

@PrototypeComponent ("imChatSessionStartAction")
public
class ImChatSessionStartAction
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
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RandomLogic randomLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

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

		ImChatSessionStartRequest startRequest =
			dataFromJson.fromJson (
				ImChatSessionStartRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatSessionStartAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				requestContext.requestIntegerRequired (
					"imChatId"));

		// lookup customer

		ImChatCustomerRec customer =
			imChatCustomerHelper.findByEmail (
				imChat,
				startRequest.email ());

		if (customer == null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-invalid")

				.message (
					"No customer with that email address exists");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// verify password

		if (
			notEqual (
				customer.getPassword (),
				startRequest.password ())
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"password-invalid")

				.message (
					"The supplied password is not correct");

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// create session

		ImChatSessionRec session =
			imChatSessionHelper.insert (
				imChatSessionHelper.createInstance ()

			.setImChatCustomer (
				customer)

			.setSecret (
				randomLogic.generateLowercase (20))

			.setActive (
				true)

			.setStartTime (
				transaction.now ())

			.setUpdateTime (
				transaction.now ())

			.setUserAgentText (
				textHelper.findOrCreateMapNull (
					startRequest.userAgent ()))

			.setIpAddress (
				requestContext.realIp ())

		);

		customer

			.setActiveSession (
				session)

			.setLastSession (
				transaction.now ());

		// create response

		ImChatSessionStartSuccess successResponse =
			new ImChatSessionStartSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				imChatApiLogic.customerData (
					customer))

			.conversation (
				customer.getCurrentConversation () != null
					? imChatApiLogic.conversationData (
						customer.getCurrentConversation ())
					: null);

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				successResponse);

	}

}
