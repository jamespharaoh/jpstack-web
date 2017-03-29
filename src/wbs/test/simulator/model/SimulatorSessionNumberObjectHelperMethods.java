package wbs.test.simulator.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface SimulatorSessionNumberObjectHelperMethods {

	SimulatorSessionNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			NumberRec number);

}