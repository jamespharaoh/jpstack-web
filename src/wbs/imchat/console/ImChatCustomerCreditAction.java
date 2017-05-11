package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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
	@NamedDependency ("imChatCustomerCreditFormContextBuilder")
	FormContextBuilder <ImChatCustomerCreditRequest> formContextBuilder;

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
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// process form fields

			FormContext <ImChatCustomerCreditRequest> formContext =
				formContextBuilder.build (
					transaction,
					emptyMap ());

			ImChatCustomerCreditRequest request =
				formContext.object ();

			request

				.customer (
					imChatCustomerHelper.findFromContextRequired (
						transaction));

			formContext.update (
					transaction);

			if (formContext.errors ()) {

				formContext.reportErrors (
					transaction);

				return null;

			}

			// create credit log

			imChatCustomerCreditHelper.insert (
				transaction,
				imChatCustomerCreditHelper.createInstance ()

				.setImChatCustomer (
					request.customer ())

				.setIndex (
					request.customer ().getNumCredits ())

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

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
