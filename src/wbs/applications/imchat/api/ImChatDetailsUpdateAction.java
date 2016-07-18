package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.underscoreToHyphen;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.applications.imchat.model.ImChatCustomerDetailValueObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerDetailValueRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
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
import wbs.platform.event.logic.EventLogic;

@PrototypeComponent ("imChatDetailsUpdateAction")
public
class ImChatDetailsUpdateAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerDetailValueObjectHelper imChatCustomerDetailValueHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	EventLogic eventLogic;

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

		ImChatDetailsUpdateRequest detailsUpdateRequest =
			dataFromJson.fromJson (
				ImChatDetailsUpdateRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatDetailsUpdateAction.handler ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"imChatId")));

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				detailsUpdateRequest.sessionSecret ());

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		if (

			isNull (
				session)

			|| ! session.getActive ()

			|| notEqual (
				customer.getImChat (),
				imChat)

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

		// update details

		for (
			ImChatCustomerDetailTypeRec detailType
				: imChat.getCustomerDetailTypes ()
		) {

			if (
				doesNotContain (
					detailsUpdateRequest.details ().keySet (),
					underscoreToHyphen (
						detailType.getCode ()))
			) {
				continue;
			}

			String stringValue =
				detailsUpdateRequest.details ().get (
					underscoreToHyphen (
						detailType.getCode ()));

			ImChatCustomerDetailValueRec detailValue =
				customer.getDetails ().get (
					detailType.getId ());

			if (
				isNotNull (
					detailValue)
			) {

				if (
					equal (
						detailValue.getValue (),
						stringValue)
				) {
					continue;
				}

				detailValue

					.setValue (
						stringValue);

			} else {

				detailValue =
					imChatCustomerDetailValueHelper.insert (
						imChatCustomerDetailValueHelper.createInstance ()

					.setImChatCustomer (
						customer)

					.setImChatCustomerDetailType (
						detailType)

					.setValue (
						stringValue)

				);

				customer.getDetails ().put (
					detailType.getId (),
					detailValue);

			}

			eventLogic.createEvent (
				"im_chat_customer_detail_updated",
				customer,
				detailType,
				stringValue);

		}

		customer

			.setDetailsCompleted (
				true);

		// create response

		ImChatDetailsUpdateSuccess successResponse =
			new ImChatDetailsUpdateSuccess ()

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
