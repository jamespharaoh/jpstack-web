package wbs.smsapps.forwarder.model;

import java.util.List;

public
interface ForwarderMessageOutDaoMethods {

	ForwarderMessageOutRec findByOtherId (
			ForwarderRec forwarder,
			String otherId);

	List<ForwarderMessageOutRec> findPendingLimit (
			ForwarderRec forwarder,
			int maxResults);

}