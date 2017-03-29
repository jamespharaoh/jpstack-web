package wbs.smsapps.broadcast.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberObjectHelperMethods {

	BroadcastNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			BroadcastRec broadcast,
			NumberRec number);

	List <BroadcastNumberRec> findOrCreateMany (
			TaskLogger parentTaskLogger,
			BroadcastRec broadcast,
			List <NumberRec> numbers);

}