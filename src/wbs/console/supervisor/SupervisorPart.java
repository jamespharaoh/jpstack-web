package wbs.console.supervisor;

import static wbs.utils.collection.CollectionUtils.collectionHasLessThanTwoElements;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.OptionalUtils.optionalOrElseOptional;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localTime;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
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
import wbs.console.part.PagePartFactory;
import wbs.console.reporting.StatsConsoleLogic;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPart")
public
class SupervisorPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

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

	String localUrl;

	List <String> supervisorConfigNames;
	List <SupervisorConfig> supervisorConfigs;

	Optional <String> selectedSupervisorConfigName;

	Optional <SupervisorConfig> supervisorConfig;

	ObsoleteDateField dateField;

	DateTime startTime;
	DateTime endTime;

	StatsPeriod statsPeriod;

	Map <String, Object> statsConditions;
	Map <String, StatsDataSet> statsDataSets;

	List <PagePart> pageParts =
		Collections.emptyList ();

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			localUrl =
				requestContext.resolveLocalUrl (
					stringFormat (
						"/%s",
						fileName ()));

			prepareSupervisorConfig (
				transaction);

			prepareDate (
				transaction);

			if (
				optionalIsPresent (
					supervisorConfig)
			) {

				createStatsPeriod (
					transaction);

				createStatsConditions (
					transaction);

				createStatsDataSets (
					transaction);

				createPageParts (
					transaction);

			}

		}

	}

	void prepareSupervisorConfig (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareSupervisorConfig");

		) {

			if (fixedSupervisorConfigName != null) {

				selectedSupervisorConfigName =
					optionalOfFormat (
						fixedSupervisorConfigName);

				supervisorConfigNames =
					Collections.singletonList (
						fixedSupervisorConfigName);

			} else {

				supervisorConfigNames =
					supervisorHelper.getSupervisorConfigNames (
						transaction);

				ImmutableList.Builder <SupervisorConfig>
					supervisorConfigsBuilder =
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
					optionalOrElseOptional (
						requestContext.parameter (
							"config"),
						() -> ifThenElse (
							supervisorConfigNames.isEmpty (),
							() -> optionalAbsent (),
							() -> optionalOf (
								listFirstElementRequired (
									supervisorConfigNames))));

				if (

					optionalIsPresent (
						selectedSupervisorConfigName)

					&& doesNotContain (
						supervisorConfigNames,
						optionalGetRequired (
							selectedSupervisorConfigName))

				) {
					throw new RuntimeException ();
				}

			}

			if (
				optionalIsPresent (
					selectedSupervisorConfigName)
			) {

				supervisorConfig =
					optionalOf (
						consoleManager.supervisorConfig (
							optionalGetRequired (
								selectedSupervisorConfigName)));

				if (
					optionalIsNotPresent (
						supervisorConfig)
				) {

					throw new RuntimeException (
						stringFormat (
							"Supervisor config not found: %s",
							optionalGetRequired (
								selectedSupervisorConfigName)));

				}

			} else {

				supervisorConfig =
					optionalAbsent ();

			}

		}

	}

	void prepareDate (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareDate");

		) {

			dateField =
				ObsoleteDateField.parse (
					requestContext.parameterOrNull (
						"date"));

			if (dateField.date == null) {

				requestContext.addError (
					"Invalid date");

				return;

			}

			if (
				optionalIsPresent (
					supervisorConfig)
			) {

				startTime =
					dateField.date

					.toDateTime (
						localTime (
							ifNull (
								supervisorConfig.get ().spec ().offsetHours (),
								0l)),
						consoleUserHelper.timezone (
							transaction));

				endTime =
					dateField.date

					.plusDays (1)

					.toDateTime (
						localTime (
							ifNull (
								supervisorConfig.get ().spec ().offsetHours (),
								0l)),
						consoleUserHelper.timezone (
							transaction));

			} else {

				startTime =
					dateField.date

					.toDateTime (
						localTime (0l),
						consoleUserHelper.timezone (
							transaction));

				endTime =
					dateField.date

					.plusDays (1)

					.toDateTime (
						localTime (0l),
						consoleUserHelper.timezone (
							transaction));

			}

		}

	}

	void createStatsPeriod (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStatsPeriod");

		) {

			statsPeriod =
				statsConsoleLogic.createStatsPeriod (
					StatsGranularity.hour,
					startTime,
					endTime,
					ifNull (
						supervisorConfig.get ().spec ().offsetHours (),
						0l));

		}

	}

	void createStatsConditions (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStatsConditions");

		) {

			ImmutableMap.Builder <String, Object> conditionsBuilder =
				ImmutableMap.builder ();

			for (
				Object object
					: supervisorConfig.get ().spec ().builders ()
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

	}

	private
	void createStatsDataSets (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStatsDataSets");

		) {

			ImmutableMap.Builder <String, StatsDataSet> dataSetsBuilder =
				ImmutableMap.builder ();

			for (
				Object object
					: supervisorConfig.get ().spec ().builders ()
			) {

				if (! (object instanceof SupervisorDataSetSpec))
					continue;

				SupervisorDataSetSpec supervisorDataSetSpec =
					(SupervisorDataSetSpec)
					object;

				StatsProvider statsProvider =
					componentManager.getComponentRequired (
						transaction,
						supervisorDataSetSpec.providerBeanName (),
						StatsProvider.class);

				StatsDataSet statsDataSet =
					statsProvider.getStats (
						transaction,
						statsPeriod,
						statsConditions);

				dataSetsBuilder.put (
					supervisorDataSetSpec.name (),
					statsDataSet);

			}

			statsDataSets =
				dataSetsBuilder.build ();

		}

	}

	private
	void createPageParts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createPageParts");

		) {

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
				PagePartFactory pagePartFactory
					: supervisorConfig.get ().pagePartFactories ()
			) {

				PagePart pagePart =
					pagePartFactory.buildPagePart (
						transaction);

				pagePart.setup (
					transaction,
					partParameters);

				pagePart.prepare (
					transaction);

				pagePartsBuilder.add (
					pagePart);

			}

			pageParts =
				pagePartsBuilder.build ();

		}

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContent");

		) {

			for (
				PagePart pagePart
					: pageParts
			) {

				pagePart.renderHtmlHeadContent (
					transaction);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			renderLinks (
				transaction);

			renderDateForm (
				transaction);

			ObsoleteDateLinks.dailyBrowserParagraph (
				formatWriter,
				localUrl,
				requestContext.formData (),
				dateField.date);

			renderTimeChangeWarning (
				transaction);

			if (
				collectionIsEmpty (
					pageParts)
			) {

				htmlParagraphWriteFormat (
					"There is no configured data to display on this page.");

			} else {

				for (
					PagePart pagePart
						: pageParts
				) {

					pagePart.renderHtmlBodyContent (
						transaction);

				}

			}

		}

	}

	private
	void renderLinks (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderLinks");

		) {

			if (
				collectionHasLessThanTwoElements (
					supervisorConfigNames)
			) {
				return;
			}

			htmlParagraphOpen (
				htmlClassAttribute (
					"links"));

			for (
				SupervisorConfig oneSupervisorConfig
					: supervisorConfigs
			)  {

				htmlLinkWrite (
					stringFormat (
						"%s",
						localUrl,
						"?config=%u",
						oneSupervisorConfig.name (),
						"&date=%u",
						dateField.text),
					oneSupervisorConfig.label (),
					htmlClassAttribute (
						presentInstances (
							optionalIf (
								stringEqualSafe (
									oneSupervisorConfig.name (),
									optionalGetRequired (
										selectedSupervisorConfigName)),
								() -> "selected"))));

			}

			htmlParagraphClose ();

		}

	}

	private
	void renderDateForm (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"htmlFormOpenGetAction");

		) {

			htmlFormOpenGetAction (
				localUrl);

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"Date<br>");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"date\"",
				" value=\"%h\"",
				dateField.text,
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"ok\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

		}

	}

	private
	void renderTimeChangeWarning (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTimeChangeWarning");

		) {

			// warning if time change

			long hoursInDay =
				new Duration (
					startTime,
					endTime)
				.toStandardHours ().getHours ();

			if (
				integerEqualSafe (
					hoursInDay,
					24l)
			) {
				return;
			}

			htmlParagraphOpen (
				htmlClassAttribute (
					"warning"));

			formatWriter.writeLineFormat (
				"This day contains %h ",
				integerToDecimalString (
					hoursInDay),
				"hours due to a time change from %h ",
				consoleUserHelper.timezoneString (
					transaction,
					startTime),
				"to %h",
				consoleUserHelper.timezoneString (
					transaction,
					endTime));

			htmlParagraphClose ();

		}

	}

}
