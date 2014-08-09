package wbs.platform.postgresql.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.postgresql.model.PostgresqlMaintenanceFrequency;
import wbs.platform.postgresql.model.PostgresqlMaintenanceRec;

@PrototypeComponent ("postgresqlMaintenanceListPart")
public
class PostgresqlMaintenanceListPart
	extends AbstractPagePart {

	@Inject
	PostgresqlMaintenanceConsoleHelper postgresqlMaintenanceHelper;

	@Inject
	TimeFormatter timeFormatter;

	Map<
		PostgresqlMaintenanceFrequency,
		Set<PostgresqlMaintenanceRec>
	>
	maintenancesByFrequency =
		new TreeMap<
			PostgresqlMaintenanceFrequency,
			Set<PostgresqlMaintenanceRec>
		> ();

	@Override
	public
	void prepare () {

		for (PostgresqlMaintenanceFrequency frequency
				: PostgresqlMaintenanceFrequency.values ()) {

			maintenancesByFrequency.put (
				frequency,
				new TreeSet<PostgresqlMaintenanceRec> ());

		}

		for (PostgresqlMaintenanceRec maintenance
				: postgresqlMaintenanceHelper.findAll ()) {

			maintenancesByFrequency
				.get (maintenance.getFrequency ())
				.add (maintenance);

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Seq</th>\n",
			"<th>Command</th>\n",
			"<th>Last run</th>\n",
			"<th>Last duration</th>\n",
			"</tr>\n");

		for (PostgresqlMaintenanceFrequency frequency
				: PostgresqlMaintenanceFrequency.values ()) {

			printFormat (
				"<tr class=\"sep\">\n");

			long totalDuration = 0;

			for (PostgresqlMaintenanceRec maintenance
					: maintenancesByFrequency.get (
						frequency)) {

				if (maintenance.getLastDuration () == null)
					continue;

				totalDuration +=
					maintenance.getLastDuration ();

			}

			printFormat (
				"<tr style=\"font-weight: bold\">\n",

				"<td colspan=\"3\">%h</td>\n",
				frequency.getDescription (),

				"<td>%h</td>\n",
				requestContext.prettyMsInterval (
					totalDuration),

				"</tr>\n");

			for (PostgresqlMaintenanceRec postgresqlMaintenance
					: maintenancesByFrequency.get (
						frequency)) {

				printFormat (
					"%s\n",
					Html.magicTr (
						requestContext.resolveContextUrl (
							stringFormat (
								"/postgresqlMaintenance",
								"/%u",
								postgresqlMaintenance.getId (),
								"/postgresqlMaintenance.summary")),
						false),

					"<td>%h</td>\n",
					postgresqlMaintenance.getSequence (),

					"<td>%h</td>\n",
					postgresqlMaintenance.getCommand (),

					"<td>%h</td>\n",
					ifNull (
						timeFormatter.instantToTimestampString (
							dateToInstant (
								postgresqlMaintenance.getLastRun ())),
						"-"),

					"<td>%h</td>\n",
					ifNull (
						requestContext.prettyMsInterval (
							postgresqlMaintenance.getLastDuration ()),
						"-"),

					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
