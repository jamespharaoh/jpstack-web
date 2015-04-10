package wbs.platform.reporting.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("userStatsGrouper")
public
class UserStatsGrouper
	implements StatsGrouper {

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Set<Object> getGroups (
			StatsDataSet dataSet) {

		return new HashSet<Object> (
			dataSet.indexValues ().get ("userId"));

	}

	@Override
	public
	String tdForGroup (
			Object group) {

		UserRec user =
			userHelper.find (
				(Integer) group);

		return consoleObjectManager.tdForObjectMiniLink (
			user);

	}

	@Override
	public
	List<Object> sortGroups (
			Set<Object> groups) {

		List<UserRec> users =
			new ArrayList<UserRec> (
				groups.size ());

		for (Object group : groups) {

			Integer userId =
				(Integer) group;

			users.add (
				userHelper.find (userId));

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
