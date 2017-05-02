package wbs.smsapps.forwarder.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ForwarderMessageOutDaoMethods {

	ForwarderMessageOutRec findByOtherId (
			Transaction parentTransaction,
			ForwarderRec forwarder,
			String otherId);

	List <ForwarderMessageOutRec> findPendingLimit (
			Transaction parentTransaction,
			ForwarderRec forwarder,
			Long maxResults);

}