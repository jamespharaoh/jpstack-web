package wbs.console.combo;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

import com.google.common.base.Optional;

@PrototypeComponent ("contextFormActionAction")
@Accessors (fluent = true)
public
class ContextFormActionAction<FormState>
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	FormFieldSet fields;

	@Getter @Setter
	ConsoleFormActionHelper<FormState> formActionHelper;

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
	Responder goReal ()
		throws ServletException {

		FormState formState =
			formActionHelper.constructFormState ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		formActionHelper.updatePassiveFormState (
			formState);

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				fields,
				formState);

		if (updateResultSet.errorCount () > 0) {
			return null;
		}

		Optional<Responder> responder =
			formActionHelper.processFormSubmission (
				transaction,
				formState);

		return responder.orNull ();

	}

}
