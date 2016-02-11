package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;

import lombok.Cleanup;

import com.google.common.collect.ImmutableMap;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("imChatCustomerCreditAction")
public
class ImChatCustomerCreditAction
	extends ConsoleAction {

	// implementation

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject @Named
	ConsoleModule imChatCustomerConsoleModule;

	@Inject
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	@Inject
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"imChatCustomerCreditResponder");

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
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// process form fields

		FormFieldSet formFields =
			imChatCustomerConsoleModule.formFieldSets ().get (
				"credit-request");

		ImChatCustomerCreditRequest request =
			ifNull (

			(ImChatCustomerCreditRequest)
			requestContext.request (
				"imChatCustomerCreditRequest"),

			new ImChatCustomerCreditRequest ()

		);

		request.customer (
			imChatCustomerHelper.find (
				requestContext.stuffInt (
					"imChatCustomerId")));

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				requestContext,
				formFields,
				request,
				ImmutableMap.of (),
				"credit");

		if (updateResultSet.errorCount () > 0) {

			requestContext.request (
				"imChatCustomerCreditUpdateResults",
				updateResultSet);

			formFieldLogic.reportErrors (
				requestContext,
				updateResultSet,
				"credit");

			return null;

		}

		// create credit log

		imChatCustomerCreditHelper.insert (
			imChatCustomerCreditHelper.createInstance ()

			.setImChatCustomer (
				request.customer ())

			.setIndex (
				(int) (long)
				request.customer ().getNumCredits ())

			.setTimestamp (
				transaction.now ())

			.setUser (
				myUser)

			.setReason (
				request.reason ())

			.setCreditAmount (
				request.creditAmount ())

			.setCreditBalanceBefore (
				request.customer ().getBalance ())

			.setCreditBalanceAfter (
				+ request.customer ().getBalance ()
				+ request.creditAmount ())

			.setBillAmount (
				request.billAmount ())

		);

		// update customer

		request.customer ()

			.setNumCredits (
				request.customer.getNumCredits () + 1)

			.setBalance (
				+ request.customer ().getBalance ()
				+ request.creditAmount ());

		// complete transaction

		transaction.commit ();

		requestContext.addNotice (
			"Customer credit applied");

		return null;

	}

}
