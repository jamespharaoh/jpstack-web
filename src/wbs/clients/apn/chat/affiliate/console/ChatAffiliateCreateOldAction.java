package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.core.console.ChatConsoleHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.keyword.console.ChatKeywordConsoleHelper;
import wbs.clients.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.clients.apn.chat.keyword.model.ChatKeywordRec;
import wbs.clients.apn.chat.scheme.console.ChatSchemeConsoleHelper;
import wbs.clients.apn.chat.scheme.console.ChatSchemeKeywordConsoleHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivDataLoader;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.sms.keyword.logic.KeywordLogic;

@Accessors (fluent = true)
@PrototypeComponent ("chatAffiliateCreateOldAction")
public
class ChatAffiliateCreateOldAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	ChatKeywordConsoleHelper chatKeywordHelper;

	@Inject
	ChatSchemeConsoleHelper chatSchemeHelper;

	@Inject
	ChatSchemeKeywordConsoleHelper chatSchemeKeywordHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	KeywordLogic keywordLogic;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UserPrivDataLoader privDataLoader;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatAffiliateCreateResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		String name =
			requestContext.parameterRequired (
				"name");

		String code =
			simplifyToCodeRequired (
				name);

		Integer chatSchemeId =
			toInteger (
				requestContext.parameterRequired (
					"chatScheme"));

		if (chatSchemeId == null) {

			requestContext.addError (
				"Please fill in the form properly");

			return null;

		}

		// check keywords
		for (int i = 0; i < 3; i++) {

			String keyword =
				requestContext.parameterRequired (
					"keyword" + i);

			if (
				isEmpty (
					keyword)
			) {
				continue;
			}

			if (! keywordLogic.checkKeyword (keyword)) {

				requestContext.addError (
					"keyword can only contain letters and numbers");

				return null;

			}

			if (
				isEmpty (
					requestContext.parameterRequired (
						"joinType" + i))
			) {

				requestContext.addError (
					"Please specify a join type for each keyword");

				return null;

			}

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatAffiliateCreateOldAction.goReal ()",
				this);

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInt (
					"chatId"));

		ChatSchemeRec chatScheme =
			chatSchemeHelper.findRequired (
				chatSchemeId);

		if (chatScheme.getChat () != chat)
			throw new RuntimeException ();

		// check permissions

		if (! privChecker.canRecursive (
				chatScheme,
				"affiliate_create")) {

			requestContext.addError ("Access denied");

			return null;

		}

		// check uniqueness of code

		Optional<ChatAffiliateRec> existingChatAffiliateOptional =
			chatAffiliateHelper.findByCode (
				chatScheme,
				code);

		if (
			isPresent (
				existingChatAffiliateOptional)
		) {

			requestContext.addError (
				stringFormat (
					"That name is already in use."));

			return null;

		}

		// check uniqueness of keywords

		for (int i = 0; i < 3; i++) {

			String keyword =
				requestContext.parameterRequired (
					"keyword" + i);

			if (
				isEmpty (
					keyword)
			) {
				continue;
			}

			Optional<ChatKeywordRec> existingChatKeywordOptional =
				chatKeywordHelper.findByCode (
					chat,
					keyword);

			if (
				isPresent (
					existingChatKeywordOptional)
			) {

				requestContext.addError (
					stringFormat (
						"Global keyword already exists: %s",
						keyword));

				return null;
			}

			Optional<ChatSchemeKeywordRec> existingChatSchemeKeywordOptional =
				chatSchemeKeywordHelper.findByCode (
					chatScheme,
					keyword);

			if (
				isPresent (
					existingChatSchemeKeywordOptional)
			) {

				requestContext.addError (
					stringFormat (
						"Keyword already exists: %s",
						keyword));

				return null;

			}

		}

		// create chat affiliate

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.insert (
				chatAffiliateHelper.createInstance ()

			.setChatScheme (
				chatScheme)

			.setName (
				name)

			.setCode (
				code)

			.setDescription (
				requestContext.parameterRequired (
					"description"))

		);

		// create keywords

		for (int index = 0; index < 3; index ++) {

			String keyword =
				requestContext.parameterRequired (
					"keyword" + index);

			if (
				isEmpty (
					keyword)
			) {
				continue;
			}

			chatSchemeKeywordHelper.insert (
				chatSchemeKeywordHelper.createInstance ()

				.setChatScheme (
					chatScheme)

				.setKeyword (
					keyword)

				.setJoinType (
					toEnum (
						ChatKeywordJoinType.class,
						requestContext.parameterRequired (
							"joinType" + index)))

				.setJoinGender (
					toEnum (
						Gender.class,
						requestContext.parameterRequired (
							"gender" + index)))

				.setJoinOrient (
					toEnum (
						Orient.class,
						requestContext.parameterRequired (
							"orient" + index)))

				.setJoinChatAffiliate (
					chatAffiliate)

			);

		}

		transaction.commit ();

		// set up our new tab context

		requestContext.addNotice (
			"Chat affiliate created");

		requestContext.setEmptyFormData ();

		privChecker.refresh ();

		ConsoleContext targetContext =
			consoleManager.context (
				"chatAffiliate",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + chatAffiliate.getId ());

		return responder ("chatAffiliateSettingsResponder");

	}

}
