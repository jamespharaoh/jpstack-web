package wbs.apn.chat.core.logic;

import wbs.sms.number.core.model.NumberRec;

public
interface ChatNumberReportLogic {

	boolean isNumberReportSuccessful (
			NumberRec number);

	boolean isNumberReportPastPermanentDeliveryConstraint (
			NumberRec number);

}
