package wbs.console.formaction;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
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
class ConsoleFormActionPart <FormState>
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
	ConsoleFormActionHelper <FormState> formActionHelper;

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

	// state

	FormState formState;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		formState =
			formActionHelper.constructFormState ();

		formActionHelper.updatePassiveFormState (
			formState);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		// write heading

		if (
			isNotNull (
				heading)
		) {

			htmlHeadingTwoWrite (
				heading);

		}

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
			formatWriter);

		// write form

		formFieldLogic.outputFormTable (
			requestContext,
			formatWriter,
			formFields,
			optionalAbsent (),
			formState,
			emptyMap (),
			"post",
			requestContext.resolveLocalUrl (
				localFile),
			submitLabel,
			FormType.perform,
			name);

	}

}
