package wbs.smsapps.manualresponder.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberDaoMethods {

	ManualResponderNumberRec find (
			Transaction parentTransaction,
			ManualResponderRec manualResponder,
			NumberRec number);

	List <Long> searchIds (
			Transaction parentTransaction,
			ManualResponderNumberSearch search);

}