package wbs.smsapps.manualresponder.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface ManualResponderRequestDaoMethods {

	List <ManualResponderRequestRec> findRecentLimit (
			Transaction parentTransaction,
			ManualResponderRec manualResponder,
			NumberRec number,
			Long maxResults);

	Criteria searchCriteria (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <Long> searchIds (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	// service report

	Criteria searchServiceReportCriteria (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <Long> searchServiceReportIds (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <ManualResponderServiceReport> searchServiceReports (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <Optional <ManualResponderServiceReport>> searchServiceReports (
			Transaction parentTransaction,
			ManualResponderRequestSearch search,
			List <Long> ids);

	// operator report

	Criteria searchOperatorReportCriteria (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <Long> searchOperatorReportIds (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <ManualResponderOperatorReport> searchOperatorReports (
			Transaction parentTransaction,
			ManualResponderRequestSearch search);

	List <Optional <ManualResponderOperatorReport>> searchOperatorReports (
			Transaction parentTransaction,
			ManualResponderRequestSearch search,
			List <Long> ids);

}