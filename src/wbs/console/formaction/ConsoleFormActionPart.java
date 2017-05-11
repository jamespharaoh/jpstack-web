package wbs.console.formaction;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFormActionPart")
public
class ConsoleFormActionPart <FormState, History>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	ConsoleFormActionHelper <FormState, History> helper;

	@Getter @Setter
	FormContextBuilder <FormState> actionFormContextBuilder;

	@Getter @Setter
	String name;

	@Getter @Setter
	String heading;

	@Getter @Setter
	String helpText;

	@Getter @Setter
	String submitLabel;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	String historyHeading;

	@Getter @Setter
	FormContextBuilder <History> historyFormContextBuilder;

	// state

	FormContext <FormState> actionFormContext;
	FormContext <History> historyFormContext;

	List <History> history;

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

			// prepare action

			Map <String, Object> formHints =
				helper.formHints (
					transaction);

			FormState formState =
				helper.constructFormState (
					transaction);

			helper.updatePassiveFormState (
				transaction,
				formState);

			actionFormContext =
				actionFormContextBuilder.build (
					transaction,
					formHints,
					formState);

			// prepare history

			if (
				isNotNull (
					historyFormContextBuilder)
			) {

				historyFormContext =
					historyFormContextBuilder.build (
						transaction,
						formHints,
						helper.history (
							transaction));

			}

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

			// write heading

			htmlHeadingTwoWrite (
				heading);

			// write help

			if (
				isNotNull (
					helpText)
			) {

				htmlParagraphWrite (
					helpText);

			}

			// write preamble

			helper.writePreamble (
				transaction,
				formatWriter,
				isNotNull (
					submitLabel));

			// write form

			if (
				isNotNull (
					submitLabel)
			) {

				actionFormContext.outputFormTable (
					transaction,
					"post",
					requestContext.resolveLocalUrl (
						localFile),
					submitLabel);

			}

			// write history

			if (
				isNotNull (
					historyFormContext)
			) {

				htmlHeadingTwoWrite (
					historyHeading);

				historyFormContext.outputListTable (
					transaction,
					true);

			}

		}

	}

}
