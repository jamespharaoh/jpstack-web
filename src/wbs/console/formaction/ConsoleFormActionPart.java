package wbs.console.formaction;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			formState =
				formActionHelper.constructFormState (
					transaction);

			formActionHelper.updatePassiveFormState (
				transaction,
				formState);

			formHints =
				formActionHelper.formHints (
					transaction);

			updateResultSet =
				genericCastUnchecked (
					requestContext.request (
						"console-form-action-update-result-set"));

			history =
				formActionHelper.history (
					transaction);

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

			formActionHelper.writePreamble (
				transaction,
				formatWriter,
				isNotNull (
					submitLabel));

			// write form

			if (
				isNotNull (
					submitLabel)
			) {

				formFieldLogic.outputFormTable (
					transaction,
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
					transaction,
					formatWriter,
					historyFields,
					optionalAbsent (),
					history,
					formHints,
					true);

			}

		}

	}

}
