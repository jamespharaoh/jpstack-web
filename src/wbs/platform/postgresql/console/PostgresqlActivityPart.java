package wbs.platform.postgresql.console;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.postgresql.model.PostgresqlStatActivityObjectHelper;
import wbs.platform.postgresql.model.PostgresqlStatActivityRec;

@PrototypeComponent ("postgresqlActivityPart")
public
class PostgresqlActivityPart
	extends AbstractPagePart {

	@Inject
	PostgresqlStatActivityObjectHelper postgresqlStatActivityHelper;

	List<PostgresqlStatActivityRec> activeStatActivities;

	List<PostgresqlStatActivityRec> idleStatActivities;

	@Override
	public
	void prepare () {

		List<PostgresqlStatActivityRec> allStatActivities =
			postgresqlStatActivityHelper.findAll ();

		activeStatActivities =
			new ArrayList<PostgresqlStatActivityRec> ();

		idleStatActivities =
			new ArrayList<PostgresqlStatActivityRec> ();

		for (PostgresqlStatActivityRec statActivity
				: allStatActivities) {

			if (equal (
					statActivity.getCurrentQuery (),
					"<IDLE>")) {

				idleStatActivities.add (
					statActivity);

			} else {

				activeStatActivities.add (
					statActivity);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		doList (
			activeStatActivities);

		printFormat (
			"<h2>Idle</h2>\n");

		doList (
			idleStatActivities);

	}

	void doList (
			List<PostgresqlStatActivityRec> statActivities) {

		if (statActivities.size () == 0) {

			printFormat (
				"<p>(none)</p>\n");

			return;

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>PID</th>\n",
			"<th>Database</th>\n",
			"<th>User</th>\n",
			"<th>Query</th>\n",
			"</tr>\n");

		for (PostgresqlStatActivityRec statActivity
				: statActivities) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				statActivity.getProcessPid (),

				"<td>%h</td>\n",
				statActivity.getDatabaseName (),

				"<td>%h</td>\n",
				statActivity.getUserName (),

				"<td>%h</td>\n",
				statActivity.getCurrentQuery (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
