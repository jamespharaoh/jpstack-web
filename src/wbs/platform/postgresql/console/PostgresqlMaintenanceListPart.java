package wbs.platform.postgresql.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.console.html.JqueryScriptRef;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.postgresql.model.PostgresqlMaintenanceFrequency;
import wbs.platform.postgresql.model.PostgresqlMaintenanceRec;

@PrototypeComponent ("postgresqlMaintenanceListPart")
public
class PostgresqlMaintenanceListPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	PostgresqlMaintenanceConsoleHelper postgresqlMaintenanceHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	Map<
		PostgresqlMaintenanceFrequency,
		Set<PostgresqlMaintenanceRec>
	>
	maintenancesByFrequency =
		new TreeMap<
			PostgresqlMaintenanceFrequency,
			Set<PostgresqlMaintenanceRec>
		> ();

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

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
				new TreeSet<PostgresqlMaintenanceRec> ());

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

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Seq</th>\n",
			"<th>Command</th>\n",
			"<th>Last run</th>\n",
			"<th>Last duration</th>\n",
			"</tr>\n");

		for (
			PostgresqlMaintenanceFrequency frequency
				: PostgresqlMaintenanceFrequency.values ()
		) {

			printFormat (
				"<tr class=\"sep\">\n");

			long totalDuration = 0;

			for (
				PostgresqlMaintenanceRec maintenance
					: maintenancesByFrequency.get (
						frequency)
			) {

				if (maintenance.getLastDuration () == null)
					continue;

				totalDuration +=
					maintenance.getLastDuration ();

			}

			printFormat (
				"<tr style=\"font-weight: bold\">\n");

			printFormat (
				"<td colspan=\"3\">%h</td>\n",
				frequency.getDescription ());

			printFormat (
				"<td>%h</td>\n",
				requestContext.prettyMsInterval (
					totalDuration));

			printFormat (
				"</tr>\n");

			for (
				PostgresqlMaintenanceRec postgresqlMaintenance
					: maintenancesByFrequency.get (
						frequency)
			) {

				printFormat (
					"<tr",
					" class=\"magic-table-row\"",

					" data-target-href=\"%h\"",
					requestContext.resolveContextUrl (
						stringFormat (
							"/postgresqlMaintenance",
							"/%u",
							postgresqlMaintenance.getId (),
							"/postgresqlMaintenance.summary")),

					">\n");

				printFormat (
					"<td>%h</td>\n",
					postgresqlMaintenance.getSequence ());

				printFormat (
					"<td>%h</td>\n",
					postgresqlMaintenance.getCommand ());

				printFormat (
					"<td>%h</td>\n",
					ifNull (
						timeFormatter.instantToTimestampString (
							timeFormatter.defaultTimezone (),
							dateToInstant (
								postgresqlMaintenance.getLastRun ())),
						"-"));

				printFormat (
					"<td>%h</td>\n",
					ifNull (
						requestContext.prettyMsInterval (
							postgresqlMaintenance.getLastDuration ()),
						"-"));

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
