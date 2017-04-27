package wbs.console.formaction;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFormActionPart")
public
class ConsoleFormActionPart <FormState, History>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	FormFieldSet <FormState> formFields;

	@Getter @Setter
	ConsoleFormActionHelper <FormState, History> formActionHelper;

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
	FormFieldSet <History> historyFields;

	// state

	FormState formState;

	Map <String, Object> formHints;

	Optional <UpdateResultSet> updateResultSet;

	List <History> history;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		formState =
			formActionHelper.constructFormState ();

		formActionHelper.updatePassiveFormState (
			formState);

		formHints =
			formActionHelper.formHints ();

		updateResultSet =
			genericCastUnchecked (
				requestContext.request (
					"console-form-action-update-result-set"));

		history =
			formActionHelper.history ();

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

			formActionHelper.writePreamble (
				taskLogger,
				formatWriter,
				isNotNull (
					submitLabel));

			// write form

			if (
				isNotNull (
					submitLabel)
			) {

				formFieldLogic.outputFormTable (
					taskLogger,
					requestContext,
					formatWriter,
					formFields,
					updateResultSet,
					formState,
					formHints,
					"post",
					requestContext.resolveLocalUrl (
						localFile),
					submitLabel,
					FormType.perform,
					name);

			}

			// write history

			if (
				isNotNull (
					historyFields)
			) {

				htmlHeadingTwoWrite (
					historyHeading);

				formFieldLogic.outputListTable (
					taskLogger,
					formatWriter,
					historyFields,
					history,
					formHints,
					true);

			}

		}

	}

}
