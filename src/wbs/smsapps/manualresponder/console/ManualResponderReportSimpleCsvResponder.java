package wbs.smsapps.manualresponder.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
import wbs.smsapps.manualresponder.console.ManualResponderReportSimplePart.SearchForm;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderReportSimpleCsvResponder")
public
class ManualResponderReportSimpleCsvResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject @Named
	ConsoleModule manualResponderReportConsoleModule;

	@Inject
	ConsoleRequestContext requestContext;

	// state

	PrintWriter out;

	List<ManualResponderReportRec> reports;
	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;
	SearchForm searchForm;

	// implementation

	@Override
	protected
	void setup ()
		throws IOException {

		out =
			requestContext.writer ();

	}

	@Override
	public
	void prepare () {

		searchFormFieldSet =
			manualResponderReportConsoleModule.formFieldSets ().get (
				"simpleReportSearch");

		resultsFormFieldSet =
			manualResponderReportConsoleModule.formFieldSets ().get (
				"simpleReportCsv");

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

		reports =
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()));

	}

	@Override
	public
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/csv");

		requestContext.setHeader (
			"Content-Disposition",
			"attachment;filename=report.csv");

	}

	@Override
	public
	void goContent ()
		throws IOException {

		formFieldLogic.outputCsvHeadings (
			out,
			resultsFormFieldSet);

		for (
			ManualResponderReportRec report
				: reports
		) {

			formFieldLogic.outputCsvRow (
				out,
				resultsFormFieldSet,
				report);

		}

		out.flush ();

	}

}