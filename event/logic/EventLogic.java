package wbs.platform.event.logic;

import wbs.framework.logging.TaskLogger;

import wbs.platform.event.model.EventRec;

public
interface EventLogic {

	EventRec createEvent (
			TaskLogger parentTaskLogger,
			String typeCode);

	EventRec createEvent (
			TaskLogger parentTaskLogger,
			String typeCode,
			Object... objects);

	public final static
	long integerEventLinkType = -1l;

	public final static
	long booleanEventLinkType = -2l;

	public final static
	long instantEventLinkType = -3l;

	public final static
	long durationEventLinkType = -4l;

}