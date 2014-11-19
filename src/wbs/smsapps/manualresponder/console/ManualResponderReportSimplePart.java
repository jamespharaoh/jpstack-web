package wbs.smsapps.manualresponder.console;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderReportSimplePart")
public
class ManualResponderReportSimplePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject @Named
	ConsoleModule manualResponderReportConsoleModule;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	// state

	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;

	SearchForm searchForm;

	List<ManualResponderReportRec> reports;

	// implementation

	@Override
	public
	void prepare () {

		searchFormFieldSet =
			manualResponderReportConsoleModule.formFieldSets ().get (
				"simpleReportSearch");

		resultsFormFieldSet =
			manualResponderReportConsoleModule.formFieldSets ().get (
				"simpleReportResults");

		// get search form

		LocalDate today =
			LocalDate.now ();

		Interval todayInterval =
			today.toInterval ();

		searchForm =
			new SearchForm ()

			.start (
				todayInterval.getStart ().toInstant ())

			.end (
				todayInterval.getEnd ().toInstant ());

		formFieldLogic.update (
			searchFormFieldSet,
			searchForm);

		// perform search

		reports =
			manualResponderReportHelper.find (
				new Interval (
					searchForm.start (),
					searchForm.end ()));

	}

	@Override
	public
	void goBodyStuff () {

		// search form

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			searchFormFieldSet,
			searchForm);

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",
			"<td><input",
			" type=\"submit\"",
			" value=\"search\"",
			"></td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		// results

		printFormat (
			"<table class=\"list\">\n");

		formFieldLogic.outputTableHeadings (
			out,
			resultsFormFieldSet);

		for (ManualResponderReportRec report
				: reports) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCells (
				out,
				resultsFormFieldSet,
				report,
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	// search form

	@Accessors (fluent = true)
	@Data
	public static
	class SearchForm {

		Instant start;
		Instant end;

	}

}
