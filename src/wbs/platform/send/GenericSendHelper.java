package wbs.platform.send;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;

public
interface GenericSendHelper <
	Service extends Record <Service>,
	Job extends Record <Job>,
	Item extends Record <Item>
> {

	String parentTypeName ();

	String name ();
	String itemNamePlural ();

	ObjectHelper <Job> jobHelper ();
	ObjectHelper <Item> itemHelper ();

	List <Job> findSendingJobs ();

	List <Job> findScheduledJobs (
			Instant now);

	List <Item> findItemsLimit (
			Service service,
			Job job,
			int maxResults);

	Service getService (
			Job job);

	Instant getScheduledTime (
			Service service,
			Job job);

	boolean jobScheduled (
			Service service,
			Job job);

	boolean jobSending (
			Service service,
			Job job);

	boolean jobConfigured (
			Service service,
			Job job);

	void sendStart (
			TaskLogger parentTaskLogger,
			Service service,
			Job job);

	boolean verifyItem (
			TaskLogger parentTaskLogger,
			Service service,
			Job job,
			Item item);

	void rejectItem (
			Service service,
			Job job,
			Item item);

	void sendItem (
			TaskLogger parentTaskLogger,
			Service service,
			Job job,
			Item item);

	void sendComplete (
			TaskLogger parentTaskLogger,
			Service service,
			Job job);

}
