package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;

import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatConditionsAcceptApiAction")
public
class ImChatConditionsAcceptApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			ImChatConditionsAcceptRequest conditionsAcceptRequest =
				dataFromJson.fromJson (
					ImChatConditionsAcceptRequest.class,
					requestContext.requestBodyString ());

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup session

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					conditionsAcceptRequest.sessionSecret ());

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			if (

				isNull (
					session)

				|| not (
					session.getActive ())

				|| referenceNotEqualWithClass (
					ImChatRec.class,
					customer.getImChat (),
					imChat)

			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			if (! customer.getAcceptedTermsAndConditions ()) {

				// accept terms and conditions

				customer

					.setAcceptedTermsAndConditions (
						true);

				eventLogic.createEvent (
					transaction,
					"im_chat_customer_conditions_accepted",
					customer);

			}

			// create response

			ImChatConditionsAcceptSuccess successResponse =
				new ImChatConditionsAcceptSuccess ()

				.customer (
					imChatApiLogic.customerData (
						transaction,
						customer));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					successResponse)

			));

		}

	}

}
