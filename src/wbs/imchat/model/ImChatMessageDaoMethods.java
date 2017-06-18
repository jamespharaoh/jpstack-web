package wbs.imchat.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

import wbs.framework.database.Transaction;

public
interface ImChatMessageDaoMethods {

	Criteria searchCriteria (
			Transaction parentTransaction,
			ImChatMessageStatsSearch search);

	Criteria searchOperatorReportCriteria (
			Transaction parentTransaction,
			ImChatMessageStatsSearch search);

	List <Long> searchOperatorReportIds (
			Transaction parentTransaction,
			ImChatMessageStatsSearch search);

	List <Optional <ImChatOperatorReport>> findOperatorReports (
			Transaction parentTransaction,
			ImChatMessageStatsSearch search,
			List <Long> ids);

	List <ImChatMessageUserStats> searchMessageUserStats (
			Transaction parentTransaction,
			ImChatMessageStatsSearch search);

}
