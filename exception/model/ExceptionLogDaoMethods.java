package wbs.platform.exception.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.logging.TaskLogger;

public
interface ExceptionLogDaoMethods {

	Long countWithAlert ();

	Long countWithAlertAndFatal ();

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			ExceptionLogSearch search);

	List <ExceptionLogRec> findOldLimit (
			Instant cutoffTime,
			Long maxResults);

}