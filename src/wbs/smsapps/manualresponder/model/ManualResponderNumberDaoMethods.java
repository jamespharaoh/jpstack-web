package wbs.smsapps.manualresponder.model;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberDaoMethods {

	ManualResponderNumberRec find (
			ManualResponderRec manualResponder,
			NumberRec number);

}