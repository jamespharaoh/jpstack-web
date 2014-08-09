package wbs.psychic.user.admin.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.psychic.bill.model.PsychicCreditLogRec;
import wbs.psychic.bill.model.PsychicUserAccountRec;
import wbs.psychic.user.core.console.PsychicUserConsoleHelper;
import wbs.psychic.user.core.model.PsychicUserRec;

@PrototypeComponent ("psychicUserAdminCreditAction")
public
class PsychicUserAdminCreditAction
	extends ConsoleAction {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldsLogic;

	@Inject
	PsychicUserConsoleHelper psychicUserHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	//@Inject @Named
	FormFieldSet psychicUserAdminCreditFormFieldSet;

	@Override
	public
	Responder backupResponder () {
		return responder ("psychicUserAdminCreditResponder");
	}

	@Override
	protected
	Responder goReal () {

		// populate form object

		PsychicUserAdminCreditForm psychicUserAdminCreditForm =
			new PsychicUserAdminCreditForm ();

		UpdateResultSet updateResultSet =
			formFieldsLogic.update (
				psychicUserAdminCreditFormFieldSet,
				psychicUserAdminCreditForm);

		if (updateResultSet.errorCount () > 0) {

			formFieldsLogic.reportErrors (
				updateResultSet);

			return null;

		}

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		PsychicUserRec psychicUser =
			psychicUserHelper.find (
				requestContext.stuffInt ("psychicUserId"));

		// update account

		PsychicUserAccountRec psychicUserAccount =
			psychicUser.getAccount ();

		psychicUserAccount
			.setCreditAdminFree (
				+ psychicUserAccount.getCreditAdminFree ()
				+ psychicUserAdminCreditForm.getCreditAmount ()
				- psychicUserAdminCreditForm.getPaymentAmount ())
			.setCreditAdminPaid (
				+ psychicUserAccount.getCreditAdminPaid ()
				+ psychicUserAdminCreditForm.getPaymentAmount ());

		// create log

		objectManager.insert (
			new PsychicCreditLogRec ()
				.setPsychicUser (psychicUser)
				.setTimestamp (transaction.now ())
				.setUser (myUser)
				.setCreditAmount (
					psychicUserAdminCreditForm.getCreditAmount ())
				.setPaymentAmount (
					psychicUserAdminCreditForm.getPaymentAmount ())
				.setDetailsText (
					textHelper.findOrCreate (
						psychicUserAdminCreditForm.getDetails ()))
				.setGift (false));

		// finish up

		transaction.commit ();

		requestContext.addNotice (
			"Credit applied");

		requestContext.setEmptyFormData ();

		return null;

	}

}
