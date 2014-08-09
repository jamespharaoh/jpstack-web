package wbs.platform.supervisor;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.html.ObsoleteDateLinks;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;
import wbs.platform.reporting.console.StatsConsoleLogic;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsGranularity;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPart")
public
class SupervisorPart
	extends AbstractPagePart {

	@Inject
	ApplicationContext applicationContext;

	@Inject
	StatsConsoleLogic statsConsoleLogic;

	@Getter @Setter
	SupervisorPageSpec supervisorPageSpec;

	@Getter @Setter
	List<Provider<PagePart>> pagePartFactories;

	ObsoleteDateField dateField;

	StatsPeriod statsPeriod;

	Map<String,StatsDataSet> statsDataSets;

	List<PagePart> pageParts =
		Collections.emptyList ();

	@Override
	public
	void prepare () {

		// interpret date

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

		if (dateField.date == null) {

			requestContext.addError (
				"Invalid date");

			return;

		}

		Instant startTime =
			dateToInstant (dateField.date);

		Instant endTime =
			startTime
				.toDateTime ()
				.plusDays (1)
				.toInstant ();

		// create stats period

		statsPeriod =
			statsConsoleLogic.createStatsPeriod (
				StatsGranularity.hour,
				startTime,
				endTime);

		// create conditions

		ImmutableMap.Builder<String,Object> conditionsBuilder =
			ImmutableMap.<String,Object>builder ();

		for (Object object
				: supervisorPageSpec.builders ()) {

			if (! (object instanceof SupervisorConditionSpec))
				continue;

			SupervisorConditionSpec supervisorConditionSpec =
				(SupervisorConditionSpec) object;

			conditionsBuilder.put (
				supervisorConditionSpec.name (),
				requestContext.stuff (
					supervisorConditionSpec.stuffKey ()));

		}

		Map<String,Object> conditions =
			conditionsBuilder.build ();

		// retrieve data sets

		ImmutableMap.Builder<String,StatsDataSet> dataSetsBuilder =
			ImmutableMap.<String,StatsDataSet>builder ();

		for (Object object
				: supervisorPageSpec.builders ()) {

			if (! (object instanceof SupervisorDataSetSpec))
				continue;

			SupervisorDataSetSpec supervisorDataSetSpec =
				(SupervisorDataSetSpec) object;

			StatsProvider statsProvider =
				applicationContext.getBean (
					supervisorDataSetSpec.providerBeanName (),
					StatsProvider.class);

			StatsDataSet statsDataSet =
				statsProvider.getStats (
					statsPeriod,
					conditions);

			dataSetsBuilder.put (
				supervisorDataSetSpec.name (),
				statsDataSet);

		}

		statsDataSets =
			dataSetsBuilder.build ();

		// setup page parts

		Map<String,Object> partParameters =
			ImmutableMap.<String,Object>builder ()
				.put ("statsPeriod", statsPeriod)
				.put ("statsDataSetsByName", statsDataSets)
				.build ();

		ImmutableList.Builder<PagePart> pagePartsBuilder =
			ImmutableList.<PagePart>builder ();

		for (Provider<PagePart> pagePartFactory
				: pagePartFactories) {

			PagePart pagePart =
				pagePartFactory.get ();

			pagePart.setup (
				partParameters);

			pagePart.prepare ();

			pagePartsBuilder.add (
				pagePart);

		}

		pageParts =
			pagePartsBuilder.build ();

	}

	@Override
	public
	void goHeadStuff () {

		for (PagePart pagePart
				: pageParts) {

			pagePart.goHeadStuff ();

		}

	}

	@Override
	public
	void goBodyStuff () {

		String localUrl =
			requestContext.resolveLocalUrl (
				stringFormat (
					"/%s",
					supervisorPageSpec.fileName ()));

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"%h\"",
			localUrl,
			">\n");

		printFormat (
			"<p>Date<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		ObsoleteDateLinks.dailyBrowser (
			out,
			localUrl,
			requestContext.getFormData (),
			dateField.date);

		// page parts

		for (PagePart pagePart
				: pageParts) {

			pagePart.goBodyStuff ();

		}

	}

}
