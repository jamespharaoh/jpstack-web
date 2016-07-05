package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.ifNotPresent;
import static wbs.framework.utils.etc.Misc.optionalCast;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;

import lombok.Cleanup;

import com.google.common.base.Optional;
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
import wbs.platform.user.console.UserConsoleLogic;

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
	UserConsoleLogic userConsoleLogic;

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

		// process form fields

		FormFieldSet formFields =
			imChatCustomerConsoleModule.formFieldSets ().get (
				"credit-request");

		ImChatCustomerCreditRequest request =
			ifNotPresent (

			optionalCast (
				ImChatCustomerCreditRequest.class,
				requestContext.request (
					"imChatCustomerCreditRequest")),

			Optional.of (
				new ImChatCustomerCreditRequest ())

		);

		request.customer (
			imChatCustomerHelper.findOrNull (
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
				userConsoleLogic.userRequired ())

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
				+ request.creditAmount ())

			.setTotalPurchaseValue (
				+ request.customer ().getTotalPurchaseValue ()
				+ request.creditAmount ())

			.setTotalPurchasePrice (
				+ request.customer ().getTotalPurchasePrice ()
				+ request.billAmount ());

		// complete transaction

		transaction.commit ();

		requestContext.addNotice (
			"Customer credit applied");

		return null;

	}

}
