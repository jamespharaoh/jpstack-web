package wbs.framework.activitymanager;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "taskId")
@ToString (of = "taskId")
public
class Task {

	long taskId;
	Object owner;
	String taskName;

	String hostname;
	Integer processId;
	String threadName;

	Instant startTime;
	Instant endTime;

	State state;

	Map<String,Object> parameters =
		new LinkedHashMap<String,Object> ();

	public static
	enum State {

		active,
		success,
		failure,
		unknown;

	}

}
