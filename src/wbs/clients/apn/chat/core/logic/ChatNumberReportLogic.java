package wbs.clients.apn.chat.core.logic;

import wbs.sms.number.core.model.NumberRec;

public interface ChatNumberReportLogic {

	public boolean isNumberReportSuccessful (
			NumberRec number);

	boolean isNumberReportPastPermanentDeliveryConstraint (
			NumberRec number);

}
