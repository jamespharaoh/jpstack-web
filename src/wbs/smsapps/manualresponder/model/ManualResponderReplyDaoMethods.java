package wbs.smsapps.manualresponder.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ManualResponderReplyDaoMethods {

	List <ManualResponderReplyUserStats> searchUserStats (
			Transaction parentTransaction,
			ManualResponderReplyStatsSearch search);

}