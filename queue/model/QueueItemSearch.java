package wbs.platform.queue.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class QueueItemSearch
	implements Serializable {

	Long sliceId;

	Long parentTypeId;

	TextualInterval createdTime;

	Long processedUserId;

	QueueItemState state;

	boolean filter;

	Collection <Long> filterQueueIds;

}
