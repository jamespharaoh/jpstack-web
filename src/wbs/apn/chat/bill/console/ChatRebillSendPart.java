package wbs.apn.chat.bill.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

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
			optionalOrElse (
				optionalCast (
					ChatRebillSearch.class,
					requestContext.request (
						"formValues")),
				() -> new ChatRebillSearch ());

		formHints =
			ImmutableMap.<String, Object> builder ()

			.put (
				"chat",
				chatHelper.findFromContextRequired ())

			.build ();

		// get search results

		Optional <List <Long>> billSearchResultsTemp =
			genericCastUnchecked (
				requestContext.request (
					"billSearchResults"));

		billSearchResults =
			optionalMapRequired (
				billSearchResultsTemp,
				list ->
					iterableMapToList (
						chatUserHelper::findRequired,
						list));

		Optional <List <Pair <Long, String>>> nonBillSearchResultsTemp =
			genericCastUnchecked (
				requestContext.request (
					"nonBillSearchResults"));

		nonBillSearchResults =
			optionalMapRequired (
				nonBillSearchResultsTemp,
				list ->
					iterableMapToList (
						chatUserWithReason ->
							new ChatRebillNonBillResult ()
								.chatUser (
									chatUserHelper.findRequired (
										(Long)
										chatUserWithReason.getLeft ()))
								.reason (
									(String)
									chatUserWithReason.getRight ()),
						list));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWriteFormat (
				"This tool allows you to rebill users who have not performed an ",
				"action recently.");

			renderSearchForm (
				taskLogger);

			renderBillSearchResults (
				taskLogger);

			renderNonBillSearchResults (
				taskLogger);

		}

	}

	private
	void renderSearchForm (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderSearchForm");

		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chat.rebill.send"));

			htmlTableOpenDetails ();

			formFieldLogic.outputFormRows (
				taskLogger,
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
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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
				taskLogger,
				formatWriter,
				billResultsFormFields,
				billSearchResults.get (),
				emptyMap (),
				true);

		}

	}

	private
	void renderNonBillSearchResults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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
				"Found %s users who match the search criteria but who are not ",
				integerToDecimalString (
					collectionSize (
						nonBillSearchResults.get ())),
				"able to be be billed at this time, due to restrictions that are ",
				"built into the system. Some of these may be bypassed with ",
				"settings on the search form.");

			formFieldLogic.outputListTable (
				taskLogger,
				formatWriter,
				nonBillResultsFormFields,
				nonBillSearchResults.get (),
				emptyMap (),
				true);

		}

	}

}
