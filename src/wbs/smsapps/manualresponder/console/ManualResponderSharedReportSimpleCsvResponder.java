package wbs.smsapps.manualresponder.console;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.priv.PrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.smsapps.manualresponder.console.ManualResponderSharedReportSimplePart.SearchForm;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderSharedReportSimpleCsvResponder")
public
class ManualResponderSharedReportSimpleCsvResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject @Named
	ConsoleModule manualResponderSharedReportConsoleModule;

	@Inject
	PrivChecker privChecker;

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
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
				"simpleReportSearch");

		resultsFormFieldSet =
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
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
					searchForm.end ()))

			.stream ()

			.filter (report ->
				privChecker.canRecursive (
					report.getManualResponder (),
					"supervisor")
				|| privChecker.canRecursive (
					report.getProcessedByUser (),
					"manage"))

			.collect (
				Collectors.toList ());

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