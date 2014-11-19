package wbs.platform.send;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;

public
interface GenericSendHelper<
	Service extends Record<Service>,
	Job extends Record<Job>,
	Item extends Record<Item>
> {

	String name ();
	String itemNamePlural ();

	ObjectHelper<Job> jobHelper ();
	ObjectHelper<Item> itemHelper ();

	List<Job> findSendingJobs ();

	List<Job> findScheduledJobs (
			Instant now);

	List<Item> findItemsLimit (
			Service service,
			Job job,
			int maxResults);

	Service getService (
			Job job);

	Instant getScheduledTime (
			Service service,
			Job job);

	boolean isScheduled (
			Service service,
			Job job);

	boolean isSending (
			Service service,
			Job job);

	boolean isConfigured (
			Service service,
			Job job);

	void sendStart (
			Service service,
			Job job);

	void sendItem (
			Service service,
			Job job,
			Item item);

	void sendComplete (
			Service service,
			Job job);

}
