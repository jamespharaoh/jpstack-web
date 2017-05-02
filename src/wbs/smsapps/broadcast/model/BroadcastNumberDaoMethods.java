package wbs.smsapps.broadcast.model;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberDaoMethods {

	BroadcastNumberRec find (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			NumberRec number);

	List <Optional <BroadcastNumberRec>> findMany (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			List <NumberRec> numbers);

	List <BroadcastNumberRec> findAcceptedLimit (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			Long limit);

}