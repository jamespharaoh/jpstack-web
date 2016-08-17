package wbs.platform.exception.model;

import java.util.List;

import org.joda.time.Instant;

public
interface ExceptionLogDaoMethods {

	Long countWithAlert ();

	Long countWithAlertAndFatal ();

	List <Long> searchIds (
			ExceptionLogSearch search);

	List <ExceptionLogRec> findOldLimit (
			Instant cutoffTime,
			Long maxResults);

}