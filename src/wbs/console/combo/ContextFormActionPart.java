package wbs.console.combo;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
	FormFieldSet formFields;

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
	void prepare () {

		formState =
			formActionHelper.constructFormState ();

		formActionHelper.updatePassiveFormState (
			formState);

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
