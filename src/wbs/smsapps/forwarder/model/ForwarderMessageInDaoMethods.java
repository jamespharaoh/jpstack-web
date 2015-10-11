package wbs.smsapps.forwarder.model;

import java.util.List;

public
interface ForwarderMessageInDaoMethods {

	ForwarderMessageInRec findNext (
			ForwarderRec forwarder);

	List<ForwarderMessageInRec> findNexts (
			int maxResults);

	List<ForwarderMessageInRec> findPendingLimit (
			ForwarderRec forwarder,
			int maxResults);

}