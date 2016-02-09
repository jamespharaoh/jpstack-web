package wbs.smsapps.manualresponder.console;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
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

	FormatWriter formatWriter;

	List<ManualResponderReportRec> reports;
	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;
	SearchForm searchForm;

	// implementation

	@Override
	protected
	void setup ()
		throws IOException {

		formatWriter =
			new FormatWriterWriter (
				requestContext.writer ());

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
			requestContext,
			searchFormFieldSet,
			searchForm,
			ImmutableMap.of (),
			"report");

		reports =
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()));

	}

	@Override
	public
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/csv");

		requestContext.setHeader (
			"Content-Disposition",
			"attachment;filename=report.csv");

	}

	@Override
	public
	void render ()
		throws IOException {

		formFieldLogic.outputCsvHeadings (
			formatWriter,
			ImmutableList.of (
				resultsFormFieldSet));

		for (
			ManualResponderReportRec report
				: reports
		) {

			formFieldLogic.outputCsvRow (
				formatWriter,
				ImmutableList.of (
					resultsFormFieldSet),
				report,
				ImmutableMap.of ());

		}

	}

}