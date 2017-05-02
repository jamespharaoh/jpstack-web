package wbs.apn.chat.core.logic;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface ChatNumberReportLogic {

	boolean isNumberReportSuccessful (
			Transaction parentTransaction,
			NumberRec number);

	boolean isNumberReportPastPermanentDeliveryConstraint (
			Transaction parentTransaction,
			NumberRec number);

}
