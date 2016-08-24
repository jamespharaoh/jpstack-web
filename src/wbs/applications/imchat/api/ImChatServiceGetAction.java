package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.NumberUtils.parseIntegerRequired;

import javax.inject.Provider;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
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

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatServiceGetAction.handle ()",
				this);

		ImChatRec imChat =
			imChatHelper.findRequired (
				parseIntegerRequired (
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
