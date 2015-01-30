package wbs.imchat.core.api;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;

@PrototypeComponent ("imChatCustomerAction")
public
class ImChatCustomerAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

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

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// lookup session

		ImChatSessionRec session =
			imChatSessionHelper.findBySecret (
				requestContext.header ("x-session-secret"));

		if (
			session == null
			|| session.getImChatCustomer ().getImChat () != imChat
			|| ! session.getActive ()
		) {

			throw new RuntimeException ();

		}

		ImChatCustomerRec customer =
			session.getImChatCustomer ();

		// create response

		ImChatCustomerData customerData =
			imChatApiLogic.customerData (
				customer);

		return jsonResponderProvider.get ()
			.value (customerData);

	}

}
