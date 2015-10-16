package wbs.smsapps.manualresponder.console;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.UrlParams;
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

	@Inject
	ConsoleManager consoleManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;

	SearchForm searchForm;

	List<ManualResponderReportRec> reports;
	String outputTypeParam;

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
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// search form

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			formatWriter,
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

		UrlParams urlParams =
			new UrlParams ()

			.set (
				"start",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.start ()))

			.set (
				"end",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.end ()));


		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderReport.simpleCsv")),
			">Download CSV File</a></p>\n");

		printFormat (
			"<table class=\"list\">\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			resultsFormFieldSet);

		for (
			ManualResponderReportRec report
				: reports
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				resultsFormFieldSet,
				report,
			   true);

			printFormat (
				"</tr>\n");

		}


	 	printFormat (
			"</table>\n");

		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderReport.simpleCsv")),
			">Download CSV File</a></p>\n");

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
