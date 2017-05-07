package wbs.apn.chat.bill.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.presentInstancesList;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

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

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatRebillSendPart")
public
class ChatRebillSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	@Named
	ConsoleModule chatRebillConsoleModule;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	FormFieldSet <ChatRebillSearch> searchFormFields;
	FormFieldSet <ChatUserRec> billResultsFormFields;
	FormFieldSet <ChatRebillNonBillResult> nonBillResultsFormFields;

	Optional <UpdateResultSet> formUpdates;
	ChatRebillSearch formValues;
	Map <String, Object> formHints;

	Optional <List <ChatUserRec>> billSearchResults;
	Optional <List <ChatRebillNonBillResult>> nonBillSearchResults;

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

			// get form fields

			searchFormFields =
				chatRebillConsoleModule.formFieldSetRequired (
					"search",
					ChatRebillSearch.class);

			billResultsFormFields =
				chatRebillConsoleModule.formFieldSetRequired (
					"bill-results",
					ChatUserRec.class);

			nonBillResultsFormFields =
				chatRebillConsoleModule.formFieldSetRequired (
					"non-bill-results",
					ChatRebillNonBillResult.class);

			// get form data

			formUpdates =
				optionalCast (
					UpdateResultSet.class,
					requestContext.request (
						"formUpdates"));

			formValues =
				optionalOrElseRequired (
					optionalCast (
						ChatRebillSearch.class,
						requestContext.request (
							"formValues")),
					() -> new ChatRebillSearch ());

			formHints =
				ImmutableMap.<String, Object> builder ()

				.put (
					"chat",
					chatHelper.findFromContextRequired (
						transaction))

				.build ();

			// get search results

			Optional <List <Long>> billSearchResultsTemp =
				genericCastUnchecked (
					requestContext.request (
						"billSearchResults"));

			billSearchResults =
				optionalMapRequired (
					billSearchResultsTemp,
					chatUserIds ->
						presentInstancesList (
							chatUserHelper.findMany (
								transaction,
								chatUserIds)));

			Optional <List <Pair <Long, String>>> nonBillSearchResultsTemp =
				genericCastUnchecked (
					requestContext.request (
						"nonBillSearchResults"));

			nonBillSearchResults =
				optionalMapRequired (
					nonBillSearchResultsTemp,
					list ->
						iterableMapToList (
							list,
							chatUserWithReason ->
								new ChatRebillNonBillResult ()
									.chatUser (
										chatUserHelper.findRequired (
											transaction,
											(Long)
											chatUserWithReason.getLeft ()))
									.reason (
										(String)
										chatUserWithReason.getRight ())));

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

			htmlParagraphWriteFormat (
				"This tool allows you to rebill users who have not performed ",
				"an action recently.");

			renderSearchForm (
				transaction);

			renderBillSearchResults (
				transaction);

			renderNonBillSearchResults (
				transaction);

		}

	}

	private
	void renderSearchForm (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderSearchForm");

		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chat.rebill.send"));

			htmlTableOpenDetails ();

			formFieldLogic.outputFormRows (
				transaction,
				requestContext,
				formatWriter,
				searchFormFields,
				formUpdates,
				formValues,
				formHints,
				FormType.search,
				"rebill");

			htmlTableClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"search\"",
				" value=\"search users\"",
				">");

			if (

				optionalIsPresent (
					billSearchResults)

				&& collectionIsNotEmpty (
					billSearchResults.get ())

			) {

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"rebill\"",
					" value=\"rebill users\"",
					">");

			}

			htmlParagraphClose ();

			htmlFormClose ();

		}

	}

	private
	void renderBillSearchResults (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderBillSearchResults");

		) {

			if (
				optionalIsNotPresent (
					billSearchResults)
			) {
				return;
			}

			htmlHeadingTwoWrite (
				"Search results");

			htmlParagraphWriteFormat (
				"Found %s users who qualify for rebilling under the criteria ",
				integerToDecimalString (
					collectionSize (
						billSearchResults.get ())),
				"specified. Please note that these results are not saved and so ",
				"the list of actual users billed may be slightly different.");

			if (

				optionalIsPresent (
					nonBillSearchResults)

				&& collectionIsNotEmpty (
					nonBillSearchResults.get ())

			) {

				htmlParagraphWriteFormat (
					"Another %s users who did not meet the requirements for ",
					integerToDecimalString (
						collectionSize (
							nonBillSearchResults.get ())),
					"rebilling are shown further down the page.");

			}

			formFieldLogic.outputListTable (
				transaction,
				formatWriter,
				billResultsFormFields,
				optionalAbsent (),
				billSearchResults.get (),
				emptyMap (),
				true);

		}

	}

	private
	void renderNonBillSearchResults (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderNonBillSearchResults");

		) {

			if (

				optionalIsNotPresent (
					nonBillSearchResults)

				|| collectionIsEmpty (
					nonBillSearchResults.get ())

			) {
				return;
			}

			htmlHeadingTwoWrite (
				"Non-billable results");

			htmlParagraphWriteFormat (
				"Found %s ",
				integerToDecimalString (
					collectionSize (
						nonBillSearchResults.get ())),
				"users who match the search criteria but who are not able to ",
				"be be billed at this time, due to restrictions that are ",
				"built into the system. Some of these may be bypassed with ",
				"settings on the search form.");

			formFieldLogic.outputListTable (
				transaction,
				formatWriter,
				nonBillResultsFormFields,
				optionalAbsent (),
				nonBillSearchResults.get (),
				emptyMap (),
				true);

		}

	}

}
