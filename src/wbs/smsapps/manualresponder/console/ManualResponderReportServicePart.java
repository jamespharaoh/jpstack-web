package wbs.smsapps.manualresponder.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.UrlParams;
import wbs.smsapps.manualresponder.console.ManualResponderReportSimplePart.SearchForm;
import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderReportServicePart")
public
class ManualResponderReportServicePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ManualResponderObjectHelper manualResponderHelper;

	@Inject @Named
	ConsoleModule manualResponderReportConsoleModule;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;

	SearchForm searchForm;

	List<ManualResponderReportRec> reports;
	List<Integer> services;
	List<Long> num_count;

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
			requestContext,
			searchFormFieldSet,
			searchForm,
			ImmutableMap.of (),
			"report");

		// perform search

		List<Integer> services =
			new ArrayList<> ();

		List<Long> numCount =
			new ArrayList<> ();

		reports =
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()));

		for (
			ManualResponderReportRec report
				: reports
		) {

			Integer manualResponderId =
				report.getManualResponder ().getId ();

			Long num =
				report.getNum ();

			if (services.contains (manualResponderId)){

				Integer index =
					services.indexOf (
						manualResponderId);

				numCount.set (
					index,
					num + numCount.get (index));

			} else {

				services.add (
					manualResponderId);

				numCount.add (
					(long) num);

			}

		}

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
			requestContext,
			formatWriter,
			searchFormFieldSet,
			Optional.absent (),
			searchForm,
			ImmutableMap.of (),
			FormType.search,
			"report");

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
					"/manualResponderReport.serviceCsv")),
			">Download CSV File</a></p>\n");

		List<Integer> services =
			new ArrayList<> ();

		List<Long> numCount =
			new ArrayList<> ();

		for (
			ManualResponderReportRec report
				: reports
		) {

			Integer manualResponderId =
				report.getManualResponder ().getId ();

			Long num =
				report.getNum ();

			if (services.contains (manualResponderId)){

				Integer index =
					services.indexOf (manualResponderId);

				numCount.set (
					index,
					num + numCount.get (index));

			} else {

				services.add (
					manualResponderId);

				numCount.add (
					(long) num);

			}

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Service</th>\n",
			"<th>Count</th>\n",
			"</tr>\n");

		/*formFieldLogic.outputTableHeadings (
				out,
				resultsFormFieldSet);*/

		for (
			Integer service
				: services
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					manualResponderHelper.find (service)));

			printFormat (
				"<td>%h</td>\n",
				numCount.get (
					services.indexOf (service)
				).toString ());

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
					"/manualResponderReport.serviceCsv")),
			">Download CSV File</a></p>\n");

	}

}