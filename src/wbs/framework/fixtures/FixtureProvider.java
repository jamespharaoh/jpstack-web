package wbs.framework.fixtures;

import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.TaskLogger;

public
interface FixtureProvider {

	void createFixtures (
			TaskLogger parentTaskLogger,
			OwnedTransaction transaction);

}
