package wbs.imchat.console;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
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

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatCustomerSettingsPasswordAction")
public
class ImChatCustomerSettingsPasswordAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatLogic imChatLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("imChatCustomerSettingsPasswordResponder")
	Provider <WebResponder> passswordResponderProvider;

	// details

	@Override
	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return passswordResponderProvider.get ();

	}

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// find customer

			ImChatCustomerRec customer =
				imChatCustomerHelper.findFromContextRequired (
					transaction);

			// generate new password

			imChatLogic.customerPasswordGenerate (
				transaction,
				customer,
				optionalOf (
					userConsoleLogic.userRequired (
						transaction)));

			// complete transaction

			transaction.commit ();

			requestContext.addNotice (
				stringFormat (
					"New password generated: %s",
					customer.getPassword ()));

			requestContext.addNotice (
				stringFormat (
					"This password has been automatically emailed to: %s",
					customer.getEmail ()));

			requestContext.addWarning (
				stringFormat (
					"This password cannot be retrieved, please make a note of it ",
					"immediately and communicate it to the customer."));

			requestContext.addWarning (
				stringFormat (
					"This new password replaces all previous passwords for this ",
					"customer account, none of which will function from this ",
					"moment."));

			return null;

		}

	}

}
