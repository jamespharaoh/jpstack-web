package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.max;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;

import java.util.List;

import lombok.NonNull;

import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
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
	@NamedDependency ("imChatCustomerCreditHistoryFormContextBuilder")
	FormContextBuilder <ImChatCustomerCreditRec> historyFormContextBuilder;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditRequestFormContextBuilder")
	FormContextBuilder <ImChatCustomerCreditRequest> requestFormContextBuilder;

	@SingletonDependency
	@NamedDependency ("imChatCustomerCreditSummaryContextBuilder")
	FormContextBuilder <ImChatCustomerRec> summaryFormContextBuilder;

	// state

	FormContext <ImChatCustomerRec> summaryFormContext;
	FormContext <ImChatCustomerCreditRequest> requestFormContext;
	FormContext <ImChatCustomerCreditRec> historyFormContext;

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

			summaryFormContext =
				summaryFormContextBuilder.build (
					transaction,
					emptyMap ());

			requestFormContext =
				requestFormContextBuilder.build (
					transaction,
					emptyMap ());

			ImChatCustomerCreditRequest request =
				requestFormContext.object ();

			request

				.customer (
					imChatCustomerHelper.findFromContextRequired (
						transaction));

			List <ImChatCustomerCreditRec> creditHistory =
				imChatCustomerCreditHelper.findByIndexRange (
					transaction,
					request.customer (),
					max (0l, request.customer.getNumCredits () - 10l),
					request.customer.getNumCredits ());

			historyFormContext =
				historyFormContextBuilder.build (
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

			summaryFormContext.outputDetailsTable (
				transaction);

			htmlHeadingTwoWrite (
				"Apply credit");

			requestFormContext.outputFormTable (
				transaction,
				"post",
				requestContext.resolveLocalUrl (
					"/imChatCustomer.credit"),
				"apply credit");

			htmlHeadingTwoWrite (
				"Recent credit history");

			historyFormContext.outputListTable (
				transaction,
				true);

		}

	}

}
