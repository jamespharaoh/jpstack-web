package wbs.smsapps.broadcast.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberObjectHelperMethods {

	BroadcastNumberRec findOrCreate (
			BroadcastRec broadcast,
			NumberRec number);

	BroadcastNumberRec find (
			BroadcastRec broadcast,
			NumberRec number);

	List<BroadcastNumberRec> findAcceptedLimit (
			BroadcastRec broadcast,
			int limit);

}