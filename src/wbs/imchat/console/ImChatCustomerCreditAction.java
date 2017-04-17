package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.ifNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalCast;

import javax.inject.Named;
import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.Responder;

@PrototypeComponent ("imChatCustomerCreditAction")
public
class ImChatCustomerCreditAction
	extends ConsoleAction {

	// implementation

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	@Named
	ConsoleModule imChatCustomerConsoleModule;

	@SingletonDependency
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"imChatCustomerCreditResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger)
		throws ServletException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ImChatCustomerCreditAction.goReal ()",
					this);

		) {

			// process form fields

			FormFieldSet <ImChatCustomerCreditRequest> formFields =
				imChatCustomerConsoleModule.formFieldSetRequired (
					"credit-request",
					ImChatCustomerCreditRequest.class);

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
				imChatCustomerHelper.findFromContextRequired ());

			UpdateResultSet updateResultSet =
				formFieldLogic.update (
					taskLogger,
					requestContext,
					formFields,
					request,
					emptyMap (),
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
				taskLogger,
				imChatCustomerCreditHelper.createInstance ()

				.setImChatCustomer (
					request.customer ())

				.setIndex (
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

}
