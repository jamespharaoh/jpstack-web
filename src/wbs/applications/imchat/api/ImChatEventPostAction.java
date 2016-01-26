package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.millisToInstant;
import static wbs.framework.utils.etc.Misc.notEqual;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatEventObjectHelper;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatEventPostAction")
public
class ImChatEventPostAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatEventObjectHelper imChatEventHelper;

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

		ImChatEventPostRequest eventPostRequest =
			dataFromJson.fromJson (
				ImChatEventPostRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// write events

		long timestampAdjustment =
			+ transaction.now ().getMillis ()
			- eventPostRequest.timestamp ();

		for (
			ImChatEventItemRequest eventItemRequest
				: eventPostRequest.events ()
		) {

			// lookup session

			ImChatSessionRec session = null;
			ImChatCustomerRec customer = null;

			if (
				isNotNull (
					eventItemRequest.sessionSecret ())
			) {

				session =
					imChatSessionHelper.findBySecret (
						eventItemRequest.sessionSecret ());

				if (
					isNotNull (
						session)
				) {

					customer =
						session.getImChatCustomer ();

					if (

						! session.getActive ()

						|| notEqual (
							session.getImChatCustomer ().getImChat (),
							imChat)

					) {
						session = null;
						customer = null;
					}

				}

			}

			imChatEventHelper.insert (
				imChatEventHelper.createInstance ()

				.setImChat (
					imChat)

				.setTimestamp (
					transaction.now ())

				.setIndex (
					eventItemRequest.index ())

				.setClientTimestamp (
					millisToInstant (
						eventItemRequest.timestamp ()))

				.setAdjustedTimestamp (
					millisToInstant (
						+ eventItemRequest.timestamp ()
						+ timestampAdjustment))

				.setInvocationToken (
					eventPostRequest.invocationToken ())

				.setImChatSession (
					session)

				.setImChatCustomer (
					customer)

				.setSource (
					requestContext.header ("x-forwarded-for") != null
						? joinWithSeparator (
							", ",
							requestContext.header ("x-forwarded-for"),
							requestContext.request ().getRemoteHost ())
						: requestContext.request ().getRemoteHost ())

				.setType (
					eventItemRequest.type ())

				.setPayload (
					eventItemRequest.payload ().toJSONString ())

			);

		}

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				"HELLO WORLD");

	}

}
