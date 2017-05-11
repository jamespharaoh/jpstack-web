package wbs.console.formaction;

import static wbs.utils.collection.IterableUtils.iterableFindExactlyOneRequired;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.context.FormContext;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

@PrototypeComponent ("contextFormActionsAction")
@Accessors (fluent = true)
public
class ConsoleFormActionsAction
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
	List <ConsoleFormAction <?, ?>> formActions;

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

			String formName =
				requestContext.formRequired (
					"form.name");

			ConsoleFormAction <?, ?> formAction =
				iterableFindExactlyOneRequired (
					candidateFormAction ->
						stringEqualSafe (
							candidateFormAction.name (),
							formName),
					formActions);

			Object formState =
				formAction.helper ().constructFormState (
					transaction);

			formAction.helper ().updatePassiveFormState (
				transaction,
				genericCastUnchecked (
					formState));

			FormContext <?> formContext =
				formAction.actionFormContextBuilder ().build (
					transaction,
					emptyMap (),
					Collections.singletonList (
						genericCastUnchecked (
							formState)));

			formContext.update (
				transaction);

			if (formContext.errors ()) {
				return null;
			}

			Optional <Responder> responder =
				formAction.helper ().processFormSubmission (
					transaction,
					genericCastUnchecked (
						formState));

			return responder.orNull ();

		}

	}

}
