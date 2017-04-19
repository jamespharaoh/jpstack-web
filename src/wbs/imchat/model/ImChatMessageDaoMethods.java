package wbs.imchat.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;

import wbs.framework.logging.TaskLogger;

public
interface ImChatMessageDaoMethods {

	Criteria searchCriteria (
			TaskLogger parentTaskLogger,
			ImChatMessageSearch search);

	Criteria searchOperatorReportCriteria (
			TaskLogger parentTaskLogger,
			ImChatMessageSearch search);

	List <Long> searchOperatorReportIds (
			TaskLogger parentTaskLogger,
			ImChatMessageSearch search);

	List <Optional <ImChatOperatorReport>> findOperatorReports (
			TaskLogger parentTaskLogger,
			ImChatMessageSearch search,
			List <Long> ids);

}
