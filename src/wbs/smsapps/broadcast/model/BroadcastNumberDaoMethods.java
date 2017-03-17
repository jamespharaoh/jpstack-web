package wbs.smsapps.broadcast.model;

import java.util.List;

import com.google.common.base.Optional;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberDaoMethods {

	BroadcastNumberRec find (
			BroadcastRec broadcast,
			NumberRec number);

	List <Optional <BroadcastNumberRec>> findMany (
			BroadcastRec broadcast,
			List <NumberRec> numbers);

	List <BroadcastNumberRec> findAcceptedLimit (
			BroadcastRec broadcast,
			int limit);

}