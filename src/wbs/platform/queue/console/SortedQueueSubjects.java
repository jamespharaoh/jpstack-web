package wbs.platform.queue.console;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.console.QueueSubjectSorter.SubjectInfo;

@Accessors (fluent = true)
public 
class SortedQueueSubjects {

	@Getter
	List<SubjectInfo> availableSubjects;

	@Getter
	List<QueueInfo> availableQueues;

	@Getter
	List<QueueInfo> allQueues;

	@Getter
	int totalItems = 0;

	@Getter
	int waitingItems = 0;

	@Getter
	int totalAvailableItems = 0;

	@Getter
	int totalAvailableSubjects = 0;

	@Getter
	int totalClaimedItems = 0;

	@Getter
	int userClaimedItems = 0;

	@Getter
	int totalClaimedSubjects = 0;

	@Getter
	int totalUnavailableItems = 0;

	@Getter
	int totalUnavailableSubjects = 0;

}
