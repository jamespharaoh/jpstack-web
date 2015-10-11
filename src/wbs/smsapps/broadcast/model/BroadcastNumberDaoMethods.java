package wbs.smsapps.broadcast.model;

import java.util.List;

public
interface BroadcastNumberDaoMethods {

	BroadcastNumberRec findByBroadcastAndNumber (
			int broadcastId,
			int numberId);

	List<BroadcastNumberRec> findAcceptedByBroadcastLimit (
			int broadcastId,
			int limit);

}