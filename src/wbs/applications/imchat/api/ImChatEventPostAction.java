package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.millisToInstant;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.objectToString;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;

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
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
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
	ExceptionLogger exceptionLogger;

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
				"ImChatEventPostAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				Integer.parseInt (
					requestContext.requestStringRequired (
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

			// write exceptions

			if (
				equal (
					eventItemRequest.type (),
					"unhandled-error")
			) {

				JSONObject payload =
					eventItemRequest.payload ();

				exceptionLogger.logSimple (
					"external",

					objectToString (
						ifNull (
							payload.get ("source"),
							"unknown")),

					objectToString (
						ifNull (
							payload.get ("message"),
							"unknown")),

					stringFormat (

						"URL: %s\n",
						objectToString (
							ifNull (
								payload.get ("url"),
								"unknown")),

						"Line: %s\n",
						objectToString (
							ifNull (
								payload.get ("line"),
								"unknown")),

						"Column: %s\n",
						objectToString (
							ifNull (
								payload.get ("column"),
								"unknown")),

						"Message: %s\n",
						objectToString (
							ifNull (
								payload.get ("message"),
								"unknown")),

						"User agent: %s\n",
						objectToString (
							ifNull (
								payload.get ("userAgent"),
								"unknown")),

						"\n",

						"Trace:\n",
						objectToString (
							ifNull (
								payload.get ("trace"),
								""))),

					Optional.absent (),

					GenericExceptionResolution.ignoreWithThirdPartyWarning);

			} else if (
				equal (
					eventItemRequest.type (),
					"api-error")
			) {

				JSONObject payload =
					eventItemRequest.payload ();

				exceptionLogger.logSimple (
					"external",

					objectToString (
						ifNull (
							payload.get ("source"),
							"unknown")),

					objectToString (
						ifNull (
							payload.get ("error"),
							"unknown")),

					stringFormat (

						"Error: %s\n",
						objectToString (
							ifNull (
								payload.get ("error"),
								"unknown")),

						"Path: %s\n",
						objectToString (
							ifNull (
								payload.get ("path"),
								"none")),

						"User agent: %s\n",
						objectToString (
							ifNull (
								payload.get ("userAgent"),
								"unknown")),

						"Request: %s\n",
						objectToString (
							ifNull (
								payload.get ("request"),
								"none")),

						"Response: %s\n",
						objectToString (
							ifNull (
								payload.get ("response"),
								"none"))),

					Optional.absent (),

					GenericExceptionResolution.ignoreWithThirdPartyWarning);

			}

		}

		// commit and return

		transaction.commit ();

		return jsonResponderProvider.get ()

			.value (
				"HELLO WORLD");

	}

}
