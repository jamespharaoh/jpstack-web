package wbs.console.formaction;

import java.util.Map;

import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

@PrototypeComponent ("contextFormActionAction")
@Accessors (fluent = true)
public
class ConsoleFormActionAction <FormState, History>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	FormFieldSet <FormState> fields;

	@Getter @Setter
	ConsoleFormActionHelper <FormState, History> formActionHelper;

	@Getter @Setter
	String responderName;

	// implementation

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			responderName);

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger)
		throws ServletException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ContextFormActionAction.goReal ()",
					this);

		) {

			FormState formState =
				formActionHelper.constructFormState ();

			Map <String, Object> formHints =
				formActionHelper.formHints ();

			formActionHelper.updatePassiveFormState (
				formState);

			UpdateResultSet updateResultSet =
				formFieldLogic.update (
					taskLogger,
					requestContext,
					fields,
					formState,
					formHints,
					name);

			if (updateResultSet.errorCount () > 0) {

				formFieldLogic.reportErrors (
					requestContext,
					updateResultSet,
					name);

				requestContext.request (
					"console-form-action-update-result-set",
					updateResultSet);

				return null;

			}

			Optional <Responder> responder =
				formActionHelper.processFormSubmission (
					taskLogger,
					transaction,
					formState);

			return responder.orNull ();

		}

	}

}
