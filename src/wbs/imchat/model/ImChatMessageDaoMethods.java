package wbs.imchat.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

import wbs.framework.database.Transaction;

public
interface ImChatMessageDaoMethods {

	Criteria searchCriteria (
			Transaction parentTransaction,
			ImChatMessageSearch search);

	Criteria searchOperatorReportCriteria (
			Transaction parentTransaction,
			ImChatMessageSearch search);

	List <Long> searchOperatorReportIds (
			Transaction parentTransaction,
			ImChatMessageSearch search);

	List <Optional <ImChatOperatorReport>> findOperatorReports (
			Transaction parentTransaction,
			ImChatMessageSearch search,
			List <Long> ids);

}
