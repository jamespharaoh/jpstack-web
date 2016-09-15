package wbs.platform.user.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsGrouper;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.utils.string.FormatWriter;

@SingletonComponent ("userStatsGrouper")
public
class UserStatsGrouper
	implements StatsGrouper {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	Set <Object> getGroups (
			@NonNull StatsDataSet dataSet) {

		return new HashSet <Object> (
			dataSet.indexValues ().get (
				"userId"));

	}

	@Override
	public
	void writeTdForGroup (
			@NonNull FormatWriter formatWriter,
			@NonNull Object group) {

		UserRec user =
			userHelper.findRequired (
				(Long)
				group);

		consoleObjectManager.writeTdForObjectMiniLink (
			formatWriter,
			user);

	}

	@Override
	public
	List<Object> sortGroups (
			Set<Object> groups) {

		List<UserRec> users =
			new ArrayList<UserRec> (
				groups.size ());

		for (
			Object group
				: groups
		) {

			users.add (
				userHelper.findRequired (
					(Long)
					group));

		}

		Collections.sort (users);

		ArrayList<Object> ret =
			new ArrayList<Object> (
				users.size ());

		for (UserRec user : users)
			ret.add (user.getId ());

		return ret;

	}

}
