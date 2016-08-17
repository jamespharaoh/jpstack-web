package wbs.test.simulator.model;

import java.util.List;

public
interface SimulatorEventDaoMethods {

	List <SimulatorEventRec> findAfterLimit (
			Long afterId,
			Long maxResults);

}