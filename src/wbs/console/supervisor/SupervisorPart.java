package wbs.console.supervisor;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.console.reporting.StatsConsoleLogic;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.context.ApplicationContext;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPart")
public
class SupervisorPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ApplicationContext applicationContext;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	StatsConsoleLogic statsConsoleLogic;

	@SingletonDependency
	SupervisorHelper supervisorHelper;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	// properties

	@Getter @Setter
	String fileName;

	@Getter @Setter
	String fixedSupervisorConfigName;

	// state

	List<String> supervisorConfigNames;
	List<SupervisorConfig> supervisorConfigs;

	String selectedSupervisorConfigName;
	SupervisorConfig supervisorConfig;

	ObsoleteDateField dateField;

	DateTime startTime;
	DateTime endTime;

	StatsPeriod statsPeriod;

	Map<String,Object> statsConditions;
	Map<String,StatsDataSet> statsDataSets;

	List<PagePart> pageParts =
		Collections.emptyList ();

	// implementation

	@Override
	public
	void prepare () {

		prepareSupervisorConfig ();
		prepareDate ();

		if (supervisorConfig != null) {

			createStatsPeriod ();
			createStatsConditions ();
			createStatsDataSets ();

			createPageParts ();

		}

	}

	void prepareSupervisorConfig () {

		if (fixedSupervisorConfigName != null) {

			selectedSupervisorConfigName =
				fixedSupervisorConfigName;

			supervisorConfigNames =
				Collections.singletonList (
					fixedSupervisorConfigName);

		} else {

			supervisorConfigNames =
				supervisorHelper.getSupervisorConfigNames ();

			ImmutableList.Builder <SupervisorConfig> supervisorConfigsBuilder =
				ImmutableList.builder ();

			for (
				String supervisorConfigName
					: supervisorConfigNames
			) {

				SupervisorConfig supervisorConfig =
					consoleManager.supervisorConfig (
						supervisorConfigName);

				if (supervisorConfig == null) {

					throw new RuntimeException (
						stringFormat (
							"No such supervisor config: %s",
							supervisorConfigName));

				}

				supervisorConfigsBuilder.add (
					supervisorConfig);

			}

			supervisorConfigs =
				supervisorConfigsBuilder.build ();

			selectedSupervisorConfigName =
				requestContext.parameterOrDefault (
					"config",
					supervisorConfigNames.isEmpty ()
						? null
						: supervisorConfigNames.get (0));

			if (

				selectedSupervisorConfigName != null

				&& ! supervisorConfigNames.contains (
					selectedSupervisorConfigName)

			) {
				throw new RuntimeException ();
			}

		}

		if (selectedSupervisorConfigName != null) {

			supervisorConfig =
				consoleManager.supervisorConfig (
					selectedSupervisorConfigName);

			if (supervisorConfig == null) {

				throw new RuntimeException (
					stringFormat (
						"Supervisor config not found: %s",
						selectedSupervisorConfigName));

			}

		}

	}

	void prepareDate () {

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameterOrNull ("date"));

		if (dateField.date == null) {

			requestContext.addError (
				"Invalid date");

			return;

		}

		startTime =
			dateField.date

			.toDateTime (
				localTime (
					ifNull (
						supervisorConfig.spec ().offsetHours (),
						0l)),
				consoleUserHelper.timezone ());

		endTime =
			dateField.date

			.plusDays (1)

			.toDateTime (
				localTime (
					ifNull (
						supervisorConfig.spec ().offsetHours (),
						0l)),
				consoleUserHelper.timezone ());

	}

	void createStatsPeriod () {

		statsPeriod =
			statsConsoleLogic.createStatsPeriod (
				StatsGranularity.hour,
				startTime,
				endTime,
				ifNull (
					supervisorConfig.spec ().offsetHours (),
					0l));

	}

	void createStatsConditions () {

		ImmutableMap.Builder <String, Object> conditionsBuilder =
			ImmutableMap.builder ();

		for (
			Object object
				: supervisorConfig.spec ().builders ()
		) {

			if (object instanceof SupervisorConditionSpec) {

				SupervisorConditionSpec supervisorConditionSpec =
					(SupervisorConditionSpec)
					object;

				conditionsBuilder.put (
					supervisorConditionSpec.name (),
					requestContext.stuff (
						supervisorConditionSpec.stuffKey ()));

			}

			if (object instanceof SupervisorIntegerConditionSpec) {

				SupervisorIntegerConditionSpec integerConditionSpec =
					(SupervisorIntegerConditionSpec)
					object;

				conditionsBuilder.put (
					integerConditionSpec.name (),
					integerConditionSpec.value ());

			}

			if (object instanceof SupervisorIntegerInConditionSpec) {

				SupervisorIntegerInConditionSpec integerInConditionSpec =
					(SupervisorIntegerInConditionSpec) object;

				conditionsBuilder.put (
					integerInConditionSpec.name (),
					ImmutableSet.copyOf (
						integerInConditionSpec.values ()));

			}

		}

		statsConditions =
			conditionsBuilder.build ();

	}

	void createStatsDataSets () {

		ImmutableMap.Builder <String, StatsDataSet> dataSetsBuilder =
			ImmutableMap.builder ();

		for (
			Object object
				: supervisorConfig.spec ().builders ()
		) {

			if (! (object instanceof SupervisorDataSetSpec))
				continue;

			SupervisorDataSetSpec supervisorDataSetSpec =
				(SupervisorDataSetSpec)
				object;

			StatsProvider statsProvider =
				applicationContext.getComponentRequired (
					supervisorDataSetSpec.providerBeanName (),
					StatsProvider.class);

			StatsDataSet statsDataSet =
				statsProvider.getStats (
					statsPeriod,
					statsConditions);

			dataSetsBuilder.put (
				supervisorDataSetSpec.name (),
				statsDataSet);

		}

		statsDataSets =
			dataSetsBuilder.build ();

	}

	void createPageParts () {

		Map <String, Object> partParameters =
			ImmutableMap.<String, Object> builder ()

			.put (
				"statsPeriod",
				statsPeriod)

			.put (
				"statsDataSetsByName",
				statsDataSets)

			.build ();

		ImmutableList.Builder <PagePart> pagePartsBuilder =
			ImmutableList.builder ();

		for (
			Provider<PagePart> pagePartFactory
				: supervisorConfig.pagePartFactories ()
		) {

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
	void renderHtmlHeadContent () {

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.renderHtmlHeadContent ();

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		String localUrl =
			requestContext.resolveLocalUrl (
				stringFormat (
					"/%s",
					fileName ()));

		if (supervisorConfigNames.size () > 1) {

			printFormat (
				"<p",
				" class=\"links\"",
				">\n");

			for (
				SupervisorConfig oneSupervisorConfig
					: supervisorConfigs
			)  {

				printFormat (
					"<a",
					" class=\"%h\"",
					ifThenElse (
						stringEqualSafe (
							oneSupervisorConfig.name (),
							selectedSupervisorConfigName),
						() -> "selected",
						() -> ""),
					" href=\"%h\"",
					stringFormat (
						"%s",
						localUrl,
						"?config=%u",
						oneSupervisorConfig.name (),
						"&date=%u",
						dateField.text),
					">%h</a>\n",
					oneSupervisorConfig.label ());

			}

			printFormat (
				"</p>\n");

		}

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"%h\"",
			localUrl,
			">\n");

		printFormat (
			"<p>Date<br>\n");

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		ObsoleteDateLinks.dailyBrowserParagraph (
			printWriter,
			localUrl,
			requestContext.getFormData (),
			dateField.date);

		// warning if time change

		long hoursInDay =
			new Duration (
				startTime,
				endTime)
			.toStandardHours ().getHours ();

		if (
			integerNotEqualSafe (
				hoursInDay,
				24l)
		) {

			printFormat (
				"<p class=\"warning\">This day contains %h ",
				hoursInDay,
				"hours due to a time change from %h ",
				consoleUserHelper.timezoneString (
					startTime),
				"to %h</p>\n",
				consoleUserHelper.timezoneString (
					endTime));

		}

		// page parts

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.renderHtmlBodyContent ();

		}

	}

}
