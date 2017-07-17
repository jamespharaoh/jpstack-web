package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatCustomerCreditAction")
public
class ImChatCustomerCreditAction
	extends ConsoleAction {

	// implementation

	@SingletonDependency
	Database database;

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditActionFormType")
	ConsoleFormType <ImChatCustomerCreditRequest> formType;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("imChatCustomerCreditResponder")
	ComponentProvider <WebResponder> customerCreditResponderProvider;

	// details

	@Override
	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return customerCreditResponderProvider.provide (
				taskLogger);

		}

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

			// process form fields

			ConsoleForm <ImChatCustomerCreditRequest> form =
				formType.buildAction (
					transaction,
					emptyMap (),
					new ImChatCustomerCreditRequest ()

				.customer (
					imChatCustomerHelper.findFromContextRequired (
						transaction))

			);

			form.update (
				transaction);

			if (form.errors ()) {

				form.reportErrors (
					transaction);

				return null;

			}

			// create credit log

			imChatCustomerCreditHelper.insert (
				transaction,
				imChatCustomerCreditHelper.createInstance ()

				.setImChatCustomer (
					form.value ().customer ())

				.setIndex (
					form.value ().customer ().getNumCredits ())

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

				.setReason (
					form.value ().reason ())

				.setCreditAmount (
					form.value ().creditAmount ())

				.setCreditBalanceBefore (
					form.value ().customer ().getBalance ())

				.setCreditBalanceAfter (
					+ form.value ().customer ().getBalance ()
					+ form.value ().creditAmount ())

				.setBillAmount (
					form.value ().billAmount ())

			);

			// update customer

			form.value ().customer ()

				.setNumCredits (
					form.value ().customer.getNumCredits () + 1)

				.setBalance (
					+ form.value ().customer ().getBalance ()
					+ form.value ().creditAmount ())

				.setTotalPurchaseValue (
					+ form.value ().customer ().getTotalPurchaseValue ()
					+ form.value ().creditAmount ())

				.setTotalPurchasePrice (
					+ form.value ().customer ().getTotalPurchasePrice ()
					+ form.value ().billAmount ());

			// complete transaction

			transaction.commit ();

			requestContext.addNotice (
				"Customer credit applied");

			return null;

		}

	}

}
