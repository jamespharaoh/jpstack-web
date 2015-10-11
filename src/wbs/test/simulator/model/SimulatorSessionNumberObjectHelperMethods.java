package wbs.test.simulator.model;

import wbs.sms.number.core.model.NumberRec;

public
interface SimulatorSessionNumberObjectHelperMethods {

	SimulatorSessionNumberRec findOrCreate (
			NumberRec number);

}