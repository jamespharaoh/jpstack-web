package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.user.model.UserObjectHelper;
import wbs.smsapps.manualresponder.console.ManualResponderReportSimplePart.SearchForm;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderReportOperatorCsvResponder")
public
class ManualResponderReportOperatorCsvResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject @Named
	ConsoleModule manualResponderReportConsoleModule;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

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
			requestContext,
			searchFormFieldSet,
			searchForm,
			ImmutableMap.of ());

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

		List<Integer> users =
			new ArrayList<> ();

		List<Long> numCount =
			new ArrayList<> ();

		for (
			ManualResponderReportRec report
				: reports
		) {

			Integer userId =
				report.getUser ().getId ();

			Long num =
				report.getNum ();

			if (users.contains (userId)){

				Integer index =
					users.indexOf (userId);

				numCount.set (
					index,
					num + numCount.get (index));

			} else {

				users.add (
					userId);

				numCount.add (
					new Long (num));

			}

		}

		out.write (
			"\"Operator\",\"Count\"\n");

		for (
			Integer user
				: users
		) {

			out.write (
				stringFormat (
					"\"%s\",",
					userHelper.find (user).getUsername (),
					"\"%s\"\n",
					numCount.get (
						users.indexOf (user)).toString ()));

		}

		out.flush ();

	}

}