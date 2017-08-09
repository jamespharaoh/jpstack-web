package wbs.console.formaction;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFormActionsPart")
public
class ConsoleFormActionsPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFormActionPart <?, ?>>
		consoleFormActionPartProvider;

	// properties

	@Getter @Setter
	List <ConsoleFormAction <?, ?>> formActions;

	@Getter @Setter
	String localFile;

	// state

	List <PagePart> pageParts;

	// public implementation

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

			pageParts =
				formActions.stream ()

				.map (
					formAction ->
						Pair.of (
							formAction.helper ().canBePerformed (
								transaction),
							formAction))

				.filter (
					showSubmitFormAction ->
						showSubmitFormAction.getLeft ().canView ())

				.map (
					showSubmitFormAction ->
						consoleFormActionPartProvider.provide (
							transaction)

					.name (
						showSubmitFormAction.getRight ().name ())

					.helper (
						genericCastUnchecked (
							showSubmitFormAction.getRight ().helper ()))

					.actionFormType (
						genericCastUncheckedNullSafe (
							showSubmitFormAction.getRight ()
								.actionFormContextBuilder ()))

					.heading (
						showSubmitFormAction.getRight ().heading ())

					.helpText (
						showSubmitFormAction.getRight ().helpText ())

					.submitLabel (
						ifThenElse (
							showSubmitFormAction.getLeft ().canPerform (),
							() -> showSubmitFormAction.getRight ()
								.submitLabel (),
							() -> null))

					.localFile (
						localFile)

				)

				.collect (
					Collectors.toList ());

			pageParts.forEach (
				pagePart ->
					pagePart.prepare (
						transaction));

		}

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContent");

		) {

			pageParts.forEach (
				pagePart ->
					pagePart.renderHtmlHeadContent (
						transaction,
						formatWriter));

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (
				collectionIsNotEmpty (
					pageParts)
			) {

				pageParts.forEach (
					pagePart ->
						pagePart.renderHtmlBodyContent (
							transaction,
							formatWriter));

			} else {

				htmlParagraphWriteFormat (
					formatWriter,
					"No actions can be performed at this time");

			}

		}

	}

}
