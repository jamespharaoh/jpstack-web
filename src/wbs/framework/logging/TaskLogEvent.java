package wbs.framework.logging;

import java.util.List;

import org.joda.time.Instant;

public
interface TaskLogEvent {

	LogSeverity eventSeverity ();

	String eventText ();

	Instant eventStartTime ();
	Instant eventEndTime ();

	List <TaskLogEvent> eventChildren ();

}
