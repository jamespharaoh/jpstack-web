package wbs.imchat.core.api;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

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
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;

@PrototypeComponent ("imChatCustomerCreateAction")
public
class ImChatCustomerCreateAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RequestContext requestContext;

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

		ImChatCustomerCreateRequest createRequest =
			dataFromJson.fromJson (
				ImChatCustomerCreateRequest.class,
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

		ImChatCustomerRec existingCustomer =
			imChatCustomerHelper.findByEmail (
				imChat,
				createRequest.email ());

		if (existingCustomer != null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-already-exists")

				.message (
					"A customer with that email address already exists");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// create new

		ImChatCustomerRec newCustomer =
			imChatCustomerHelper.insert (
				new ImChatCustomerRec ()

			.setImChat (
				imChat)

			.setCode (
				imChatCustomerHelper.generateCode ())

			.setEmail (
				createRequest.email ())

			.setPassword (
				createRequest.password ())

		);

		// create session

		ImChatSessionRec session =
			imChatSessionHelper.insert (
				new ImChatSessionRec ()

			.setImChatCustomer (
				newCustomer)

			.setSecret (
				imChatSessionHelper.generateSecret ())

			.setActive (
				true)

			.setStartTime (
				transaction.now ())

			.setUpdateTime (
				transaction.now ())

		);

		// create response

		ImChatCustomerCreateSuccess successResponse =
			new ImChatCustomerCreateSuccess ()

			.customerCode (
				newCustomer.getCode ())

			.sessionSecret (
				session.getSecret ());

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
