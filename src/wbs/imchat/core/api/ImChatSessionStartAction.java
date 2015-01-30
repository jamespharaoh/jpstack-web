package wbs.imchat.core.api;

import static wbs.framework.utils.etc.Misc.notEqual;

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

@PrototypeComponent ("imChatSessionStartAction")
public
class ImChatSessionStartAction
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

		ImChatSessionStartRequest startRequest =
			dataFromJson.fromJson (
				ImChatSessionStartRequest.class,
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

		// lookup customer

		ImChatCustomerRec customer =
			imChatCustomerHelper.findByEmail (
				imChat,
				startRequest.email ());

		if (customer == null) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"customer-does-not-exist")

				.message (
					"No customer with that email address exists");

			return jsonResponderProvider.get ()
				.value (failureResponse);

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
					"password-incorrect")

				.message (
					"The supplied password is not correct");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// create session

		ImChatSessionRec session =
			imChatSessionHelper.insert (
				new ImChatSessionRec ()

			.setImChatCustomer (
				customer)

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

		ImChatSessionStartSuccess successResponse =
			new ImChatSessionStartSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				new ImChatCustomerData ()

				.id (
					customer.getId ())

				.code (
					customer.getCode ())

				.balance (
					customer.getBalance ())

			);

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

}
