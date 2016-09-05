package wbs.framework.activitymanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "taskId")
@ToString (of = "taskId")
public
class Task {

	long taskId;
	Task parent;
	Object owner;
	String taskType;
	String summary;

	String hostname;
	Integer processId;
	String threadName;

	Instant startTime;
	Instant endTime;

	State state;

	Map <String, String> parameters =
		new LinkedHashMap <String, String> ();

	List <Task> children =
		new ArrayList <Task> ();

	public
	Interval interval () {

		return new Interval (
			startTime (),
			endTime ());

	}

	public
	Duration duration () {

		return interval ().toDuration ();

	}

	public static
	enum State {

		active,
		success,
		failure,
		unknown;

	}

}
