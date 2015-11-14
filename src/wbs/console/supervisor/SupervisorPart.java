package wbs.console.supervisor;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
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
import wbs.framework.application.context.ApplicationContext;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPart")
public
class SupervisorPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	StatsConsoleLogic statsConsoleLogic;

	@Inject
	SupervisorHelper supervisorHelper;

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

	Instant startTime;
	Instant endTime;

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

			ImmutableList.Builder<SupervisorConfig> supervisorConfigsBuilder =
				ImmutableList.<SupervisorConfig>builder ();

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
				requestContext.parameter (
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
				requestContext.parameter ("date"));

		if (dateField.date == null) {

			requestContext.addError (
				"Invalid date");

			return;

		}

		startTime =
			dateField.date
				.toDateTimeAtStartOfDay ()
				.plusHours (
					ifNull (
						supervisorConfig.spec ().offsetHours (),
						0))
				.toInstant ();

		endTime =
			dateField.date
				.plusDays (1)
				.toDateTimeAtStartOfDay ()
				.plusHours (
					ifNull (
						supervisorConfig.spec ().offsetHours (),
						0))
				.toInstant ();

	}

	void createStatsPeriod () {

		statsPeriod =
			statsConsoleLogic.createStatsPeriod (
				StatsGranularity.hour,
				startTime,
				endTime,
				ifNull (
					supervisorConfig.spec ().offsetHours (),
					0));

	}

	void createStatsConditions () {

		ImmutableMap.Builder<String,Object> conditionsBuilder =
			ImmutableMap.<String,Object>builder ();

		for (
			Object object
				: supervisorConfig.spec ().builders ()
		) {

			if (object instanceof SupervisorConditionSpec) {

				SupervisorConditionSpec supervisorConditionSpec =
					(SupervisorConditionSpec) object;

				conditionsBuilder.put (
					supervisorConditionSpec.name (),
					requestContext.stuff (
						supervisorConditionSpec.stuffKey ()));

			}

			if (object instanceof SupervisorIntegerConditionSpec) {

				SupervisorIntegerConditionSpec integerConditionSpec =
					(SupervisorIntegerConditionSpec) object;

				conditionsBuilder.put (
					integerConditionSpec.name (),
					Integer.parseInt (
						integerConditionSpec.value ()));

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

		ImmutableMap.Builder<String,StatsDataSet> dataSetsBuilder =
			ImmutableMap.<String,StatsDataSet>builder ();

		for (
			Object object
				: supervisorConfig.spec ().builders ()
		) {

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
					statsConditions);

			dataSetsBuilder.put (
				supervisorDataSetSpec.name (),
				statsDataSet);

		}

		statsDataSets =
			dataSetsBuilder.build ();

	}

	void createPageParts () {

		Map<String,Object> partParameters =
			ImmutableMap.<String,Object>builder ()
				.put ("statsPeriod", statsPeriod)
				.put ("statsDataSetsByName", statsDataSets)
				.build ();

		ImmutableList.Builder<PagePart> pagePartsBuilder =
			ImmutableList.<PagePart>builder ();

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
					equal (
							oneSupervisorConfig.name (),
							selectedSupervisorConfigName)
						? "selected"
						: "",
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

		// page parts

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.renderHtmlBodyContent ();

		}

	}

}
