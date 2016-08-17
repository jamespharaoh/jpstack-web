package wbs.smsapps.manualresponder.model;

import java.util.List;

import org.hibernate.Criteria;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderRequestDaoMethods {

	List<ManualResponderRequestRec> findRecentLimit (
			ManualResponderRec manualResponder,
			NumberRec number,
			Long maxResults);

	Criteria searchCriteria (
			ManualResponderRequestSearch search);

	List<Long> searchIds (
			ManualResponderRequestSearch search);

	// service report

	Criteria searchServiceReportCriteria (
			ManualResponderRequestSearch search);

	List<Long> searchServiceReportIds (
			ManualResponderRequestSearch search);

	List<ManualResponderServiceReport> searchServiceReports (
			ManualResponderRequestSearch search);

	List<ManualResponderServiceReport> searchServiceReports (
			ManualResponderRequestSearch search,
			List<Long> ids);

	// operator report

	Criteria searchOperatorReportCriteria (
			ManualResponderRequestSearch search);

	List<Long> searchOperatorReportIds (
			ManualResponderRequestSearch search);

	List<ManualResponderOperatorReport> searchOperatorReports (
			ManualResponderRequestSearch search);

	List<ManualResponderOperatorReport> searchOperatorReports (
			ManualResponderRequestSearch search,
			List<Long> ids);

}