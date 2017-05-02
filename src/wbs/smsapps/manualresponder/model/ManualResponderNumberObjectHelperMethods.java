package wbs.smsapps.manualresponder.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberObjectHelperMethods {

	ManualResponderNumberRec findOrCreate (
			Transaction parentTransaction,
			ManualResponderRec manualResponder,
			NumberRec number);

}
