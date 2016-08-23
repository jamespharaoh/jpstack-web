package wbs.smsapps.manualresponder.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderNumberDaoMethods {

	ManualResponderNumberRec find (
			ManualResponderRec manualResponder,
			NumberRec number);

	List <Long> searchIds (
			ManualResponderNumberSearch search);

}