package wbs.platform.event.logic;

import java.util.Map;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface EventFixtureLogic {

	void createEvents (
			Transaction parentTransaction,
			String fixtureProviderName,
			Record <?> parent,
			Record <?> object,
			Map <String, Object> fields);

}
