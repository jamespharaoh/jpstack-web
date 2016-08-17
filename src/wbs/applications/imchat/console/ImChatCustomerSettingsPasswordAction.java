package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.applications.imchat.logic.ImChatLogic;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("imChatCustomerSettingsPasswordAction")
public
class ImChatCustomerSettingsPasswordAction
	extends ConsoleAction {

	// implementation

	@Inject
	Database database;

	@Inject
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@Inject
	ImChatLogic imChatLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"imChatCustomerSettingsPasswordResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChaCustomerSettingsPasswordAction.goReal ()",
				this);

		// find customer

		ImChatCustomerRec customer =
			imChatCustomerHelper.findRequired (
				requestContext.stuffInteger (
					"imChatCustomerId"));

		// generate new password

		imChatLogic.customerPasswordGenerate (
			customer,
			Optional.of (
				userConsoleLogic.userRequired ()));

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
