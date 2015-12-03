package wbs.platform.queue.model;

import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class QueueItemSearch {

	String createdTime;

	boolean filter;
	Collection<Integer> filterQueueIds;

}
