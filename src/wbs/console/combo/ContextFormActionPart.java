package wbs.console.combo;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("contextFormActionPart")
public
class ContextFormActionPart <FormState>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	FormFieldSet <FormState> formFields;

	@Getter @Setter
	ConsoleFormActionHelper <FormState> formActionHelper;

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

		if (helpText != null) {

			htmlParagraphWrite (
				helpText);

		}

		formFieldLogic.outputFormTable (
			requestContext,
			formatWriter,
			formFields,
			Optional.absent (),
			formState,
			ImmutableMap.of (),
			"post",
			requestContext.resolveLocalUrl (
				localFile),
			submitLabel,
			FormType.perform,
			"action");

	}

}
