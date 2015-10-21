package wbs.platform.exception.model;

import java.util.List;

import org.joda.time.Instant;

public
interface ExceptionLogDaoMethods {

	int countWithAlert ();

	int countWithAlertAndFatal ();

	List<Integer> searchIds (
			ExceptionLogSearch search);

	List<ExceptionLogRec> findOldLimit (
			Instant cutoffTime,
			int maxResults);

}