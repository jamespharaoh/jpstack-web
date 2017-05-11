package wbs.console.formaction;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	FormContextBuilder <FormState> formContextBuilder;

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
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			FormState formState =
				formActionHelper.constructFormState (
					transaction);

			Map <String, Object> formHints =
				formActionHelper.formHints (
					transaction);

			formActionHelper.updatePassiveFormState (
				transaction,
				formState);

			FormContext <FormState> formContext =
				formContextBuilder.build (
					transaction,
					formHints,
					formState);

			formContext.update (
				transaction);

			if (formContext.errors ()) {

				formContext.reportErrors (
					transaction);

				return null;

			}

			Optional <Responder> responder =
				formActionHelper.processFormSubmission (
					transaction,
					formState);

			return responder.orNull ();

		}

	}

}
