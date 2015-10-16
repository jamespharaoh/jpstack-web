package wbs.integrations.smsarena.model;

import java.util.List;

public
interface SmsArenaDlrReportLogDaoMethods {

	List<Integer> searchIds (
			SmsArenaDlrReportLogSearch smsArenaDlrReportLogSearch);

}