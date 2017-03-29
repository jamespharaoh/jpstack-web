package wbs.smsapps.manualresponder.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberObjectHelperMethods {

	ManualResponderNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			ManualResponderRec manualResponder,
			NumberRec number);

}
