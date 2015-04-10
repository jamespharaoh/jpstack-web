package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.experimental.Accessors;
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
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.priv.console.PrivDataLoader;
import wbs.sms.keyword.logic.KeywordLogic;

@Accessors (fluent = true)
@PrototypeComponent ("chatAffiliateCreateAction")
public
class ChatAffiliateCreateAction
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
	PrivChecker privChecker;

	@Inject
	PrivDataLoader privDataLoader;

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
			requestContext.parameter ("name");

		String code =
			codify (name);

		Integer chatSchemeId =
			toInteger (requestContext.parameter ("chatScheme"));

		if (chatSchemeId == null) {
			requestContext.addError ("Please fill in the form properly");
			return null;
		}

		// check keywords
		for (int i = 0; i < 3; i++) {

			String keyword =
				requestContext.parameter ("keyword" + i);

			if (keyword.length () == 0)
				continue;

			if (! keywordLogic.checkKeyword (keyword)) {

				requestContext.addError (
					"keyword can only contain letters and numbers");

				return null;

			}

			if (in (requestContext.parameter ("joinType" + i),
					null,
					"")) {

				requestContext.addError (
					"Please specify a join type for each keyword");

				return null;

			}

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		ChatSchemeRec chatScheme =
			chatSchemeHelper.find (
				chatSchemeId);

		if (chatScheme.getChat () != chat)
			throw new RuntimeException ();

		// check permissions

		if (! privChecker.can (
				chatScheme,
				"affiliate_create")) {

			requestContext.addError ("Access denied");

			return null;

		}

		// check uniqueness of code

		ChatAffiliateRec existingChatAffiliate =
			chatAffiliateHelper.findByCode (
				chatScheme,
				code);

		if (existingChatAffiliate != null) {

			requestContext.addError (
				stringFormat (
					"That name is already in use."));

			return null;

		}

		// check uniqueness of keywords

		for (int i = 0; i < 3; i++) {

			String keyword =
				requestContext.parameter ("keyword" + i);

			if (keyword.length () == 0)
				continue;

			ChatKeywordRec chatKeyword =
				chatKeywordHelper.findByCode (
					chat,
					keyword);

			if (chatKeyword != null) {

				requestContext.addError (
					stringFormat (
						"Global keyword already exists: %s",
						keyword));

				return null;
			}

			ChatSchemeKeywordRec chatSchemeKeyword =
				chatSchemeKeywordHelper.findByCode (
					chatScheme,
					keyword);

			if (chatSchemeKeyword != null) {

				requestContext.addError (
					stringFormat (
						"Keyword already exists: %s",
						keyword));

				return null;

			}

		}

		// create chat affiliate

		ChatAffiliateRec chatAffiliate =
			objectManager.insert (
				new ChatAffiliateRec ()
					.setChatScheme (chatScheme)
					.setName (name)
					.setCode (code)
					.setDescription (requestContext.parameter ("description")));

		// create keywords

		for (int index = 0; index < 3; index ++) {

			String keyword =
				requestContext.parameter ("keyword" + index);

			if (keyword.length () == 0)
				continue;

			objectManager.insert (
				new ChatSchemeKeywordRec ()
					.setChatScheme (chatScheme)
					.setKeyword (keyword)
					.setJoinType (toEnum (
						ChatKeywordJoinType.class,
						requestContext.parameter ("joinType" + index)))
					.setJoinGender (toEnum (
						Gender.class,
						requestContext.parameter ("gender" + index)))
					.setJoinOrient (toEnum (
						Orient.class,
						requestContext.parameter ("orient" + index)))
					.setJoinChatAffiliate (chatAffiliate));

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
