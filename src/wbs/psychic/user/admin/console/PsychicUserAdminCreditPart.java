package wbs.psychic.user.admin.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.part.AbstractPagePart;
import wbs.psychic.bill.console.PsychicCreditLogConsoleHelper;
import wbs.psychic.bill.model.PsychicCreditLogRec;
import wbs.psychic.user.core.console.PsychicUserConsoleHelper;
import wbs.psychic.user.core.model.PsychicUserRec;

@PrototypeComponent ("psychicUserAdminCreditPart")
public
class PsychicUserAdminCreditPart
	extends AbstractPagePart {

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	PsychicCreditLogConsoleHelper psychicCreditLogHelper;

	@Inject
	PsychicUserConsoleHelper psychicUserHelper;

	//@Inject @Named
	FormFieldSet psychicUserAdminCreditFormFieldSet;

	//@Inject @Named
	FormFieldSet psychicCreditLogFormFieldSet;

	PsychicUserRec psychicUser;
	PsychicUserAdminCreditForm psychicUserAdminCreditForm;
	List<PsychicCreditLogRec> psychicCreditLogs;

	@Override
	public
	void prepare () {

		psychicUser =
			psychicUserHelper.find (
				requestContext.stuffInt ("psychicUserId"));

		psychicUserAdminCreditForm =
			new PsychicUserAdminCreditForm ();

		/*
		if (eq (requestContext.getMethod (), "POST")) {

			try {

				psychicUserAdminCreditFields.update (
					requestContext,
					psychicUserAdminCreditForm);

			} catch (InvalidFormValue

		}
		*/

		psychicCreditLogs =
			psychicCreditLogHelper.findByParent (
				psychicUser);

		Collections.sort (
			psychicCreditLogs);

	}

	@Override
	public
	void goBodyStuff () {

		// form

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/psychicUser.admin.credit"),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			psychicUserAdminCreditFormFieldSet,
			psychicUserAdminCreditForm);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"apply credi\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			out,
			psychicCreditLogFormFieldSet);

		printFormat (
			"</tr>\n");

		for (PsychicCreditLogRec psychicCreditLog
				: psychicCreditLogs) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCells (
				out,
				psychicCreditLogFormFieldSet,
				psychicCreditLog,
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
