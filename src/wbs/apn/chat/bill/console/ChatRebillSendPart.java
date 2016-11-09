package wbs.apn.chat.bill.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	@Named
	ConsoleModule chatRebillConsoleModule;

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
	void prepare () {

		// get form fields

		searchFormFields =
			chatRebillConsoleModule.formFieldSet (
				"search",
				ChatRebillSearch.class);

		billResultsFormFields =
			chatRebillConsoleModule.formFieldSet (
				"bill-results",
				ChatUserRec.class);

		nonBillResultsFormFields =
			chatRebillConsoleModule.formFieldSet (
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
				chatHelper.findRequired (
					requestContext.stuffInteger (
						"chatId")))

			.build ();

		// get search results

		@SuppressWarnings ("unchecked")
		Optional <List <Long>> billSearchResultsTemp =
			(Optional <List <Long>>)
			requestContext.request (
				"billSearchResults");

		billSearchResults =
			optionalMapRequired (
				billSearchResultsTemp,
				list ->
					iterableMapToList (
						chatUserHelper::findRequired,
						list));

		@SuppressWarnings ("unchecked")
		Optional <List <Pair <Long, String>>> nonBillSearchResultsTemp =
			(Optional <List <Pair <Long, String>>>)
			requestContext.request (
				"nonBillSearchResults");

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
	void renderHtmlBodyContent () {

		htmlParagraphWriteFormat (
			"This tool allows you to rebill users who have not performed an ",
			"action recently.");

		renderSearchForm ();

		renderBillSearchResults ();
		renderNonBillSearchResults ();

	}

	private
	void renderSearchForm () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chat.rebill.send"));

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
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

	private
	void renderBillSearchResults () {

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
			formatWriter,
			billResultsFormFields,
			billSearchResults.get (),
			true);

	}

	private
	void renderNonBillSearchResults () {

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
			formatWriter,
			nonBillResultsFormFields,
			nonBillSearchResults.get (),
			true);

	}

}
