package wbs.test.simulator.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SimulatorSessionNumberObjectHelperMethods {

	SimulatorSessionNumberRec findOrCreate (
			Transaction parentTransaction,
			NumberRec number);

}