package wbs.console.formaction;

import static wbs.utils.collection.IterableUtils.iterableFindExactlyOneRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.List;

import javax.servlet.ServletException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	List <ConsoleFormAction <?>> formActions;

	@Getter @Setter
	String responderName;

	// implementation

	@Override
	protected
	Responder backupResponder () {

		return responder (
			responderName);

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		String formName =
			requestContext.formRequired (
				"form.name");

		ConsoleFormAction <?> formAction =
			iterableFindExactlyOneRequired (
				candidateFormAction ->
					stringEqualSafe (
						candidateFormAction.name (),
						formName),
				formActions);

		Object formState =
			formAction.helper ().constructFormState ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ContextFormActionAction.goReal ()",
				this);

		formAction.helper ().updatePassiveFormState (
			genericCastUnchecked (
				formState));

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				taskLogger,
				requestContext,
				formAction.formFields (),
				formState,
				ImmutableMap.of (),
				formName);

		if (updateResultSet.errorCount () > 0) {
			return null;
		}

		Optional <Responder> responder =
			formAction.helper ().processFormSubmission (
				taskLogger,
				transaction,
				genericCastUnchecked (
					formState));

		return responder.orNull ();

	}

}
