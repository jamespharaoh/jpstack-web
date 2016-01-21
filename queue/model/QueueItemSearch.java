package wbs.platform.queue.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class QueueItemSearch
	implements Serializable {

	Interval createdTime;

	Integer processedUserId;

	boolean filter;

	Collection<Integer> filterQueueIds;

}
