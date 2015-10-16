package wbs.integrations.smsarena.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class SmsArenaDlrReportLogHooks
	extends AbstractObjectHooks<SmsArenaDlrReportLogRec> {

	// dependencies

	@Inject
	SmsArenaDlrReportLogDao smsArenaDlrReportLogDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		SmsArenaDlrReportLogSearch smsArenaDlrReportLogSearch =
			(SmsArenaDlrReportLogSearch) search;

			return smsArenaDlrReportLogDao.searchIds (
				smsArenaDlrReportLogSearch);

	}

}