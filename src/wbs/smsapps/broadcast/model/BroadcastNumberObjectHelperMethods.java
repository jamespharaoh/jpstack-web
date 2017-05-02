package wbs.smsapps.broadcast.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface BroadcastNumberObjectHelperMethods {

	BroadcastNumberRec findOrCreate (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			NumberRec number);

	List <BroadcastNumberRec> findOrCreateMany (
			Transaction parentTransaction,
			BroadcastRec broadcast,
			List <NumberRec> numbers);

}