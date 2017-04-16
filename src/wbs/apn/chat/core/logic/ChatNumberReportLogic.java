package wbs.apn.chat.core.logic;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface ChatNumberReportLogic {

	boolean isNumberReportSuccessful (
			TaskLogger parentTaskLogger,
			NumberRec number);

	boolean isNumberReportPastPermanentDeliveryConstraint (
			TaskLogger parentTaskLogger,
			NumberRec number);

}
