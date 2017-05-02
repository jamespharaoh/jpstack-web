package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.max;
import static wbs.utils.etc.OptionalUtils.ifNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;

import java.util.List;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import lombok.NonNull;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
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
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	@Named
	ConsoleModule imChatCustomerConsoleModule;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	FormFieldSet <ImChatCustomerRec> customerFormFields;
	FormFieldSet <ImChatCustomerCreditRequest> creditFormFields;
	FormFieldSet <ImChatCustomerCreditRec> creditHistoryFormFields;

	ImChatCustomerRec customer;
	List <ImChatCustomerCreditRec> creditHistory;

	ImChatCustomerCreditRequest request;
	Optional <UpdateResultSet> updateResultSet;

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

			customerFormFields =
				imChatCustomerConsoleModule.formFieldSetRequired (
					"credit-summary",
					ImChatCustomerRec.class);

			creditFormFields =
				imChatCustomerConsoleModule.formFieldSetRequired (
					"credit-request",
					ImChatCustomerCreditRequest.class);

			creditHistoryFormFields =
				imChatCustomerConsoleModule.formFieldSetRequired (
					"credit-history",
					ImChatCustomerCreditRec.class);

			request =
				ifNotPresent (

				optionalCast (
					ImChatCustomerCreditRequest.class,
					requestContext.request (
						"imChatCustomerCreditRequest")),

				Optional.of (
					new ImChatCustomerCreditRequest ())

			);

			request.customer (
				imChatCustomerHelper.findFromContextRequired (
					transaction));

			updateResultSet =
				optionalCast (
					UpdateResultSet.class,
					requestContext.request (
						"imChatCustomerCreditUpdateResults"));

			creditHistory =
				imChatCustomerCreditHelper.findByIndexRange (
					transaction,
					request.customer (),
					max (0l, request.customer.getNumCredits () - 10l),
					request.customer.getNumCredits ());

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

			formFieldLogic.outputDetailsTable (
				transaction,
				formatWriter,
				customerFormFields,
				request.customer (),
				emptyMap ());

			htmlHeadingTwoWrite (
				"Apply credit");

			formFieldLogic.outputFormTable (
				transaction,
				requestContext,
				formatWriter,
				creditFormFields,
				updateResultSet,
				request,
				emptyMap (),
				"post",
				requestContext.resolveLocalUrl (
					"/imChatCustomer.credit"),
				"apply credit",
				FormType.perform,
				"credit");

			htmlHeadingTwoWrite (
				"Recent credit history");

			formFieldLogic.outputListTable (
				transaction,
				formatWriter,
				creditHistoryFormFields,
				optionalAbsent (),
				Lists.reverse (
					creditHistory),
				emptyMap (),
				true);

		}

	}

}
