package wbs.smsapps.manualresponder.model;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberObjectHelperMethods {

	ManualResponderNumberRec findOrCreate (
			ManualResponderRec manualResponder,
			NumberRec number);

}
