package wbs.smsapps.manualresponder.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderRequestDaoMethods {

	List<ManualResponderRequestRec> findRecentLimit (
			ManualResponderRec manualResponder,
			NumberRec number,
			Integer maxResults);

	List<Integer> searchIds (
			ManualResponderRequestSearch search);

}