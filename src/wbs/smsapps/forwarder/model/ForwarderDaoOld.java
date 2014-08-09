package wbs.smsapps.forwarder.model;

import java.util.List;

public
interface ForwarderDaoOld {

	List<ForwarderMessageInRec> findForwarderMessageInsNext (
			int maxResults);

	List<ForwarderMessageInRec> findForwarderMessageInsPendingByForwarderLimit (
			int forwarderId,
			int maxResults);

	ForwarderMessageInRec insertForwarderMessageIn (
			ForwarderMessageInRec forwarderMessageIn);

	// ================================= forwarderd message out

	ForwarderMessageOutRec findForwarderMessageOutById (
			int id);

	ForwarderMessageOutRec findForwarderMessageOutByOtherId (
			int forwarderId,
			String otherId);

	List<ForwarderMessageOutRec>
	findForwarderMessageOutsPendingByForwarderIdLimit (
			int forwarderId,
			int limit);

	ForwarderMessageOutRec insertForwarderMessageOut (
			ForwarderMessageOutRec forwarderMessageOut);

	// ================================= forwarder message out report

	ForwarderMessageOutReportRec findForwarderMessageOutReportById (
			int id);

	ForwarderMessageOutReportRec insertForwarderMessageOutReport (
			ForwarderMessageOutReportRec forwarderMessageOutReport);

}
