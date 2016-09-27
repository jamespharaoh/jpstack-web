package wbs.platform.postgresql.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

import org.joda.time.Duration;

import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.postgresql.model.PostgresqlMaintenanceFrequency;
import wbs.platform.postgresql.model.PostgresqlMaintenanceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.etc.Misc;

@PrototypeComponent ("postgresqlMaintenanceListPart")
public
class PostgresqlMaintenanceListPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	PostgresqlMaintenanceConsoleHelper postgresqlMaintenanceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	Map <
		PostgresqlMaintenanceFrequency,
		Set <PostgresqlMaintenanceRec>
	>
	maintenancesByFrequency =
		new TreeMap <> ();

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		for (
			PostgresqlMaintenanceFrequency frequency
				: PostgresqlMaintenanceFrequency.values ()
		) {

			maintenancesByFrequency.put (
				frequency,
				new TreeSet <PostgresqlMaintenanceRec> ());

		}

		for (
			PostgresqlMaintenanceRec maintenance
				: postgresqlMaintenanceHelper.findAll ()
		) {

			maintenancesByFrequency
				.get (maintenance.getFrequency ())
				.add (maintenance);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Seq",
			"Command",
			"Last run",
			"Last duration");

		for (
			PostgresqlMaintenanceFrequency frequency
				: PostgresqlMaintenanceFrequency.values ()
		) {

			htmlTableRowSeparatorWrite ();

			Duration totalDuration =
				maintenancesByFrequency.get (
					frequency)

				.stream ()

				.map (
					PostgresqlMaintenanceRec::getLastDuration)

				.filter (
					Misc::isNotNull)

				.map (
					Duration::new)

				.reduce ((left, right) ->
					left.plus (right))

				.orElse (
					Duration.ZERO);

			htmlTableRowOpen (
				htmlStyleRuleEntry (
					"font-weight",
					"bold"));

			htmlTableCellWrite (
				frequency.getDescription (),
				htmlColumnSpanAttribute (3l));

			htmlTableCellWrite (
				userConsoleLogic.prettyDuration (
					totalDuration));

			htmlTableRowClose ();

			for (
				PostgresqlMaintenanceRec postgresqlMaintenance
					: maintenancesByFrequency.get (
						frequency)
			) {

				htmlTableRowOpen (
					htmlClassAttribute (
						"magic-table-row"),
					htmlDataAttribute (
						"target-href",
						requestContext.resolveContextUrl (
							stringFormat (
								"/postgresqlMaintenance",
								"/%u",
								postgresqlMaintenance.getId (),
								"/postgresqlMaintenance.summary"))));

				htmlTableCellWrite (
					integerToDecimalString (
						postgresqlMaintenance.getSequence ()));

				htmlTableCellWrite (
					postgresqlMaintenance.getCommand ());

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						postgresqlMaintenance.getLastRun (),
						() -> userConsoleLogic.timestampWithTimezoneString (
							postgresqlMaintenance.getLastRun ())));

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						postgresqlMaintenance.getLastDuration (),
						() -> userConsoleLogic.prettyDuration (
							new Duration (
								postgresqlMaintenance.getLastDuration ()))));

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
