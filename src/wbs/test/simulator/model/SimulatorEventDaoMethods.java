package wbs.test.simulator.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface SimulatorEventDaoMethods {

	List <SimulatorEventRec> findAfterLimit (
			Transaction parentTransaction,
			Long afterId,
			Long maxResults);

}