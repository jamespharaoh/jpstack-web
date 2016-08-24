package wbs.smsapps.broadcast.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberDaoMethods {

	BroadcastNumberRec find (
			BroadcastRec broadcast,
			NumberRec number);

	List <BroadcastNumberRec> findAcceptedLimit (
			BroadcastRec broadcast,
			int limit);

}