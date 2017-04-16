package wbs.smsapps.manualresponder.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderRequestDaoMethods {

	List <ManualResponderRequestRec> findRecentLimit (
			TaskLogger parentTaskLogger,
			ManualResponderRec manualResponder,
			NumberRec number,
			Long maxResults);

	Criteria searchCriteria (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	// service report

	Criteria searchServiceReportCriteria (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <Long> searchServiceReportIds (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <ManualResponderServiceReport> searchServiceReports (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <Optional <ManualResponderServiceReport>> searchServiceReports (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search,
			List <Long> ids);

	// operator report

	Criteria searchOperatorReportCriteria (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <Long> searchOperatorReportIds (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <ManualResponderOperatorReport> searchOperatorReports (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search);

	List <Optional <ManualResponderOperatorReport>> searchOperatorReports (
			TaskLogger parentTaskLogger,
			ManualResponderRequestSearch search,
			List <Long> ids);

}