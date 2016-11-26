package wbs.framework.logging;

import java.util.List;

public
interface TaskLogEvent {

	LogSeverity eventSeverity ();

	String eventText ();

	List <TaskLogEvent> eventChildren ();

}
