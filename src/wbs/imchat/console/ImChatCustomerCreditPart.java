package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.max;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;

import java.util.List;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatCustomerCreditRec;
import wbs.imchat.model.ImChatCustomerRec;

@PrototypeComponent ("imChatCustomerCreditPart")
public
class ImChatCustomerCreditPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditCustomerFormType")
	ConsoleFormType <ImChatCustomerRec> customerFormType;

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditHistoryFormType")
	ConsoleFormType <ImChatCustomerCreditRec> historyFormType;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditActionFormType")
	ConsoleFormType <ImChatCustomerCreditRequest> requestFormType;

	// state

	ConsoleForm <ImChatCustomerRec> customerForm;
	ConsoleForm <ImChatCustomerCreditRequest> requestForm;
	ConsoleForm <ImChatCustomerCreditRec> historyForm;

	ImChatCustomerRec customer;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			customer =
				imChatCustomerHelper.findFromContextRequired (
					transaction);

			customerForm =
				customerFormType.buildResponse (
					transaction,
					emptyMap ());

			requestForm =
				requestFormType.buildResponse (
					transaction,
					emptyMap ());

			requestForm.value ()

				.customer (
					imChatCustomerHelper.findFromContextRequired (
						transaction));

			List <ImChatCustomerCreditRec> creditHistory =
				imChatCustomerCreditHelper.findByIndexRange (
					transaction,
					customer,
					max (0l, customer.getNumCredits () - 10l),
					customer.getNumCredits ());

			historyForm =
				historyFormType.buildResponse (
					transaction,
					emptyMap (),
					creditHistory);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			requestContext.flushNotices ();

			htmlHeadingTwoWrite (
				"Customer details");

			customerForm.outputDetailsTable (
				transaction);

			htmlHeadingTwoWrite (
				"Apply credit");

			requestForm.outputFormTable (
				transaction,
				"post",
				requestContext.resolveLocalUrl (
					"/imChatCustomer.credit"),
				"apply credit");

			htmlHeadingTwoWrite (
				"Recent credit history");

			historyForm.outputListTable (
				transaction,
				true);

		}

	}

}
