package wbs.imchat.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

public
interface ImChatMessageDaoMethods {

	Criteria searchCriteria (
			ImChatMessageSearch search);

	Criteria searchOperatorReportCriteria (
			ImChatMessageSearch search);

	List <Long> searchOperatorReportIds (
			ImChatMessageSearch search);

	List <Optional <ImChatOperatorReport>> findOperatorReports (
			ImChatMessageSearch search,
			List <Long> ids);

}
