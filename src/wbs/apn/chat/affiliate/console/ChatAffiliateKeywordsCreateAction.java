package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.keyword.logic.KeywordLogic;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.keyword.console.ChatKeywordConsoleHelper;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.console.ChatSchemeKeywordConsoleHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatAffiliateKeywordsCreateAction")
public
class ChatAffiliateKeywordsCreateAction
	extends ConsoleAction {

	// dependencies

	@SingletonDependency
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@SingletonDependency
	ChatKeywordConsoleHelper chatKeywordHelper;

	@SingletonDependency
	ChatSchemeKeywordConsoleHelper chatSchemeKeywordHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatAffiliateKeywordsCreateResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			String keyword =
				requestContext

				.parameterRequired (
					"keyword")

				.toLowerCase ();

			if (
				! keywordLogic.checkKeyword (
					transaction,
					keyword)
			) {

				requestContext.addError (
					"Keyword can only contain letters and numbers");

				return null;

			}

			ChatKeywordJoinType joinType =
				toEnum (
					ChatKeywordJoinType.class,
					requestContext.parameterRequired (
						"joinType"));

			if (joinType == null) {

				requestContext.addError (
					"Please specify a join type");

				return null;

			}

			Gender gender =
				toEnum (
					Gender.class,
					requestContext.parameterRequired (
						"gender"));

			Orient orient =
				toEnum (
					Orient.class,
					requestContext.parameterRequired (
						"orient"));

			ChatAffiliateRec chatAffiliate =
				chatAffiliateHelper.findFromContextRequired (
					transaction);

			ChatSchemeRec chatScheme =
				chatAffiliate.getChatScheme ();

			Optional <ChatKeywordRec> chatKeywordOptional =
				chatKeywordHelper.findByCode (
					transaction,
					chatScheme.getChat (),
					keyword);

			if (
				optionalIsPresent (
					chatKeywordOptional)
			) {

				requestContext.addError (
					"Global keyword already exists: " + keyword);

				return null;

			}

			Optional <ChatSchemeKeywordRec> chatSchemeKeywordOptional =
				chatSchemeKeywordHelper.findByCode (
					transaction,
					chatScheme,
					keyword);

			if (
				optionalIsPresent (
					chatSchemeKeywordOptional)
			) {

				requestContext.addError (
					"Keyword already exists: " + keyword);

				return null;

			}

			chatSchemeKeywordHelper.insert (
				transaction,
				chatSchemeKeywordHelper.createInstance ()

				.setChatScheme (
					chatScheme)

				.setKeyword (
					keyword)

				.setJoinType (
					joinType)

				.setJoinGender (
					gender)

				.setJoinOrient (
					orient)

				.setJoinChatAffiliate (
					chatAffiliate)

			);

			transaction.commit ();

			requestContext.addNotice (
				"Chat scheme keyword created");

			requestContext.setEmptyFormData ();

			return responder (
				"chatAffiliateKeywordsListResponder");

		}

	}

}
