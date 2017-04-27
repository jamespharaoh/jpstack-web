package wbs.console.formaction;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
	Provider <ConsoleFormActionPart <?, ?>> consoleFormActionPartProvider;

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
	void setup (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <String, Object> parameters) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			super.setup (
				taskLogger,
				parameters);

			pageParts =
				formActions.stream ()

				.map (
					formAction ->
						Pair.of (
							formAction.helper ().canBePerformed (
								taskLogger),
							formAction))

				.filter (
					showSubmitFormAction ->
						showSubmitFormAction.getLeft ().canView ())

				.map (
					showSubmitFormAction ->
						consoleFormActionPartProvider.get ()

					.name (
						showSubmitFormAction.getRight ().name ())

					.formActionHelper (
						genericCastUnchecked (
							showSubmitFormAction.getRight ().helper ()))

					.formFields (
						genericCastUncheckedNullSafe (
							showSubmitFormAction.getRight ().formFields ()))

					.heading (
						showSubmitFormAction.getRight ().heading ())

					.helpText (
						showSubmitFormAction.getRight ().helpText ())

					.submitLabel (
						ifThenElse (
							showSubmitFormAction.getLeft ().canPerform (),
							() -> showSubmitFormAction.getRight ().submitLabel (),
							() -> null))

					.localFile (
						localFile)

				)

				.collect (
					Collectors.toList ());

			pageParts.forEach (
				pagePart ->
					pagePart.setup (
						taskLogger,
						parameters));

		}

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			pageParts.forEach (
				pagePart ->
					pagePart.prepare (
						taskLogger));

		}

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlHeadContent");

		) {

			pageParts.forEach (
				pagePart ->
					pagePart.renderHtmlHeadContent (
						taskLogger));

		}

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

			if (
				collectionIsNotEmpty (
					pageParts)
			) {

				pageParts.forEach (
					pagePart ->
						pagePart.renderHtmlBodyContent (
							taskLogger));

			} else {

				htmlParagraphWriteFormat (
					"No actions can be performed at this time");

			}

		}

	}

}
