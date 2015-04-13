package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.keyword.console.ChatKeywordConsoleHelper;
import wbs.clients.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.clients.apn.chat.keyword.model.ChatKeywordRec;
import wbs.clients.apn.chat.scheme.console.ChatSchemeKeywordConsoleHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.keyword.logic.KeywordLogic;

@PrototypeComponent ("chatAffiliateKeywordsCreateAction")
public
class ChatAffiliateKeywordsCreateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@Inject
	ChatKeywordConsoleHelper chatKeywordHelper;

	@Inject
	ChatSchemeKeywordConsoleHelper chatSchemeKeywordHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	KeywordLogic keywordLogic;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatAffiliateKeywordsCreateResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		String keyword =
			requestContext
				.parameter ("keyword")
				.toLowerCase ();

		if (! keywordLogic.checkKeyword (keyword)) {

			requestContext.addError (
				"Keyword can only contain letters and numbers");

			return null;

		}

		ChatKeywordJoinType joinType =
			toEnum (
				ChatKeywordJoinType.class,
				requestContext.parameter ("joinType"));

		if (joinType == null) {
			requestContext.addError ("Please specify a join type");
			return null;
		}

		Gender gender =
			toEnum (
				Gender.class,
				requestContext.parameter ("gender"));

		Orient orient =
			toEnum (
				Orient.class,
				requestContext.parameter ("orient"));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.find (
				requestContext.stuffInt ("chatAffiliateId"));

		ChatSchemeRec chatScheme =
			chatAffiliate.getChatScheme ();

		ChatKeywordRec chatKeyword =
			chatKeywordHelper.findByCode (
				chatScheme.getChat (),
				keyword);

		if (chatKeyword != null) {

			requestContext.addError (
				"Global keyword already exists: " + keyword);

			return null;

		}

		ChatSchemeKeywordRec chatSchemeKeyword =
			chatSchemeKeywordHelper.findByCode (
				chatScheme,
				keyword);

		if (chatSchemeKeyword != null) {

			requestContext.addError (
				"Keyword already exists: " + keyword);

			return null;

		}

		objectManager.insert (
			new ChatSchemeKeywordRec ()
				.setChatScheme (chatScheme)
				.setKeyword (keyword)
				.setJoinType (joinType)
				.setJoinGender (gender)
				.setJoinOrient (orient)
				.setJoinChatAffiliate (chatAffiliate));

		transaction.commit ();

		requestContext.addNotice (
			"Chat scheme keyword created");

		requestContext.setEmptyFormData ();

		return responder ("chatAffiliateKeywordsListResponder");

	}

}
