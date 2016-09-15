package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import javax.inject.Provider;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import lombok.Cleanup;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueObjectHelper;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
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
import wbs.platform.event.logic.EventLogic;

@PrototypeComponent ("imChatDetailsUpdateAction")
public
class ImChatDetailsUpdateAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerDetailValueObjectHelper imChatCustomerDetailValueHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	EventLogic eventLogic;

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
				parseIntegerRequired (
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

			|| referenceNotEqualWithClass (
				ImChatRec.class,
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
					stringEqualSafe (
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
