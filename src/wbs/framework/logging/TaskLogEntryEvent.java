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
	LogSeverity severity;

	private final
	String text;

	private final
	Instant startTime;

	private
	Instant endTime;

	// constructors

	TaskLogEntryEvent (
			@NonNull LogSeverity severity,
			@NonNull String text) {

		this.severity =
			severity;

		this.text =
			text;

		this.startTime =
			Instant.now ();

		this.endTime =
			Instant.now ();

	}

	// accessors

	@Override
	public
	LogSeverity eventSeverity () {
		return severity;
	}

	@Override
	public
	String eventText () {
		return text;
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
