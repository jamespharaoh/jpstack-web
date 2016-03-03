package wbs.applications.imchat.api;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.currency.logic.CurrencyLogic;

@PrototypeComponent ("imChatServiceGetAction")
public
class ImChatServiceGetAction
	implements Action {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatObjectHelper imChatHelper;

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
			database.beginReadOnly (
				this);

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					requestContext.requestStringRequired (
						"imChatId")));

		// create response

		ImChatServiceData serviceData =
			new ImChatServiceData ()

			.profilePageBeforeLogin (
				imChat.getProfilePageBeforeLogin ())

			.createDetails (
				imChatApiLogic.createDetailData (
					imChat));

		// return

		return jsonResponderProvider.get ()

			.value (
				serviceData);

	}

}
