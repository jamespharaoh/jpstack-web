package wbs.smsapps.manualresponder.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.platform.user.model.UserRec;

public
interface ManualResponderReportDaoMethods {

	List<ManualResponderReportRec> findByProcessedTime (
			Interval processedTimeInterval);

	List<ManualResponderReportRec> findByProcessedTime (
			UserRec processedByUser,
			Interval processedTimeInterval);

}