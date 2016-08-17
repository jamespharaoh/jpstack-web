package wbs.applications.imchat.api;


import static wbs.framework.utils.etc.Misc.isNotEmpty;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@PrototypeComponent ("imChatCustomerCreateAction")
public
class ImChatCustomerCreateAction
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

		ImChatCustomerCreateRequest createRequest =
			dataFromJson.fromJson (
				ImChatCustomerCreateRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatCustomerCreateAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				requestContext.requestIntegerRequired (
					"imChatId"));

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

		// check email looks ok

		Matcher emailMatcher =
			emailPattern.matcher (
				createRequest.email ());

		if (! emailMatcher.matches ()) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"email-invalid")

				.message (
					"Please enter a valid email address");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// check password looks ok

		if (createRequest.password ().length () < 6) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"password-invalid")

				.message (
					"Please enter a longer password");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		// create new

		ImChatCustomerRec newCustomer =
			imChatCustomerHelper.insert (
				imChatCustomerHelper.createInstance ()

			.setImChat (
				imChat)

			.setCode (
				randomLogic.generateNumericNoZero (8))

			.setEmail (
				createRequest.email ())

			.setPassword (
				createRequest.password ())

			.setFirstSession (
				transaction.now ())

			.setLastSession (
				transaction.now ())

		);

		// update details

		Map<String,String> detailErrors =
			imChatApiLogic.updateCustomerDetails (
				newCustomer,
				createRequest.details ());

		if (
			isNotEmpty (
				detailErrors)
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"details-invalid")

				.message (
					"One or more of the details provided are invalid")

				.details (
					detailErrors);

			return jsonResponderProvider.get ()

				.value (
					failureResponse);

		}

		// create session

		ImChatSessionRec session =
			imChatSessionHelper.insert (
				imChatSessionHelper.createInstance ()

			.setImChatCustomer (
				newCustomer)

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
					createRequest.userAgent ()))

			.setIpAddress (
				requestContext.realIp ())

		);

		newCustomer

			.setActiveSession (
				session);

		// create response

		ImChatCustomerCreateSuccess successResponse =
			new ImChatCustomerCreateSuccess ()

			.sessionSecret (
				session.getSecret ())

			.customer (
				imChatApiLogic.customerData (
					newCustomer));

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()
			.value (successResponse);

	}

	final static
	Pattern emailPattern =
		Pattern.compile (
			"[^@]+@[^@]+");

}
