package wbs.framework.fixtures;

import wbs.framework.database.Transaction;

public
interface FixtureProvider {

	void createFixtures (
			Transaction parentTransaction);

}
