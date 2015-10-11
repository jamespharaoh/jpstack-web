package wbs.integrations.smsarena.model;

import java.util.List;

import wbs.integrations.smsarena.model.SmsArenaDlrReportLogRec.SmsArenaDlrReportLogSearch;

public
interface SmsArenaDlrReportLogDaoMethods {

	List<Integer> searchIds (
			SmsArenaDlrReportLogSearch smsArenaDlrReportLogSearch);

}