package wbs.smsapps.broadcast.model;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberObjectHelperMethods {

	BroadcastNumberRec findOrCreate (
			BroadcastRec broadcast,
			NumberRec number);

}