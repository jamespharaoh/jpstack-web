package wbs.framework.logging;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.NonNull;

public
class TaskLogEntryEvent
	implements TaskLogEvent {

	private final
	LogSeverity severity;

	private final
	String text;

	TaskLogEntryEvent (
			@NonNull LogSeverity severity,
			@NonNull String text) {

		this.severity =
			severity;

		this.text =
			text;

	}

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
	List <TaskLogEvent> eventChildren () {

		return emptyList ();

	}

}
