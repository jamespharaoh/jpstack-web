package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
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
import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatAffiliateKeywordsCreateResponder")
	Provider <WebResponder> createResponderProvider;

	@PrototypeDependency
	@NamedDependency ("chatAffiliateKeywordsListResponder")
	Provider <WebResponder> listResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return createResponderProvider.get ();

	}

	// implementation

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
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

			Optional <ChatKeywordJoinType> joinTypeOptional =
				toEnum (
					ChatKeywordJoinType.class,
					requestContext.parameterRequired (
						"joinType"));

			if (
				optionalIsNotPresent (
					joinTypeOptional)
			) {

				requestContext.addError (
					"Please specify a join type");

				return null;

			}

			ChatKeywordJoinType joinType =
				optionalGetRequired (
					joinTypeOptional);

			Optional <Gender> genderOptional =
				toEnum (
					Gender.class,
					requestContext.parameterRequired (
						"gender"));

			Optional <Orient> orientOptional =
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
					optionalOrNull (
						genderOptional))

				.setJoinOrient (
					optionalOrNull (
						orientOptional))

				.setJoinChatAffiliate (
					chatAffiliate)

			);

			transaction.commit ();

			requestContext.addNotice (
				"Chat scheme keyword created");

			requestContext.setEmptyFormData ();

			return listResponderProvider.get ();

		}

	}

}
