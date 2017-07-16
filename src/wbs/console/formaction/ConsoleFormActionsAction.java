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
import wbs.console.forms.core.ConsoleForm;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

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
	ComponentProvider <WebResponder> responderProvider;

	// implementation

	@Override
	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return responderProvider.provide (
				taskLogger);

		}

	}

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			String formName =
				requestContext.formRequired (
					"form.name");

			ConsoleFormAction <?, ?> formAction =
				iterableFindExactlyOneRequired (
					formActions,
					candidateFormAction ->
						stringEqualSafe (
							candidateFormAction.name (),
							formName));

			Object formState =
				formAction.helper ().constructFormState (
					transaction);

			formAction.helper ().updatePassiveFormState (
				transaction,
				genericCastUnchecked (
					formState));

			ConsoleForm <?> formContext =
				formAction.actionFormContextBuilder ().buildAction (
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

			Optional <WebResponder> responder =
				formAction.helper ().processFormSubmission (
					transaction,
					genericCastUnchecked (
						formState));

			return responder.orNull ();

		}

	}

}
