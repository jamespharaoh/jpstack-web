package wbs.smsapps.manualresponder.model;

import java.util.List;

import org.hibernate.Criteria;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderRequestDaoMethods {

	List<ManualResponderRequestRec> findRecentLimit (
			ManualResponderRec manualResponder,
			NumberRec number,
			Integer maxResults);

	Criteria searchCriteria (
			ManualResponderRequestSearch search);

	List<Integer> searchIds (
			ManualResponderRequestSearch search);

	// service report

	Criteria searchServiceReportCriteria (
			ManualResponderRequestSearch search);

	List<Integer> searchServiceReportIds (
			ManualResponderRequestSearch search);

	List<ManualResponderServiceReport> searchServiceReports (
			ManualResponderRequestSearch search);

	List<ManualResponderServiceReport> searchServiceReports (
			ManualResponderRequestSearch search,
			List<Integer> ids);

	// operator report

	Criteria searchOperatorReportCriteria (
			ManualResponderRequestSearch search);

	List<Integer> searchOperatorReportIds (
			ManualResponderRequestSearch search);

	List<ManualResponderOperatorReport> searchOperatorReports (
			ManualResponderRequestSearch search);

	List<ManualResponderOperatorReport> searchOperatorReports (
			ManualResponderRequestSearch search,
			List<Integer> ids);

}