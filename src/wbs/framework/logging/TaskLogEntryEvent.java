package wbs.framework.logging;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

public
class TaskLogEntryEvent
	implements TaskLogEvent {

	// state

	private final
	Long eventId;

	private final
	LogSeverity severity;

	private final
	CharSequence text;

	private final
	Instant startTime;

	private
	Instant endTime;

	// constructors

	TaskLogEntryEvent (
			@NonNull Long eventId,
			@NonNull LogSeverity severity,
			@NonNull CharSequence text) {

		this.eventId =
			eventId;

		this.severity =
			severity;

		this.text =
			text;

		this.startTime =
			Instant.now ();

		this.endTime =
			Instant.now ();

	}

	// task log event implementation

	@Override
	public
	Long eventId () {
		return eventId;
	}

	@Override
	public
	LogSeverity eventSeverity () {
		return severity;
	}

	@Override
	public
	String eventText () {
		return text.toString ();
	}

	@Override
	public
	Instant eventStartTime () {
		return startTime;
	}

	@Override
	public
	Instant eventEndTime () {
		return endTime;
	}

	@Override
	public
	List <TaskLogEvent> eventChildren () {
		return emptyList ();
	}

}
