package wbs.platform.supervisor;

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

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.html.ObsoleteDateLinks;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.reporting.console.StatsConsoleLogic;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsGranularity;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsProvider;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
	UserObjectHelper userHelper;

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

			UserRec myUser =
				userHelper.find (
					requestContext.userId ());

			SliceRec slice =
				myUser.getSlice ();

			supervisorConfigNames =
				slice.getSupervisorConfigNames () != null
					? ImmutableList.<String>copyOf (
						slice.getSupervisorConfigNames ().split (","))
					: Collections.<String>emptyList ();

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
				endTime);

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
	void goHeadStuff () {

		for (
			PagePart pagePart
				: pageParts
		) {

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

		ObsoleteDateLinks.dailyBrowserParagraph (
			out,
			localUrl,
			requestContext.getFormData (),
			dateField.date);

		// page parts

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.goBodyStuff ();

		}

	}

}
