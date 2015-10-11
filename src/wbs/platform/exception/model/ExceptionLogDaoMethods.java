package wbs.platform.exception.model;

import java.util.List;

public
interface ExceptionLogDaoMethods {

	int countWithAlert ();

	int countWithAlertAndFatal ();

	List<Integer> searchIds (
			ExceptionLogSearch search);

}