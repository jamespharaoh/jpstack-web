package wbs.platform.event.logic;

import wbs.platform.event.model.EventRec;

public
interface EventLogic {

	EventRec createEvent (
			String typeCode);

	EventRec createEvent (
			String typeCode,
			Object... objects);

	public final static
	long integerEventLinkType = -1l;

	public final static
	long booleanEventLinkType = -2l;

	public final static
	long instantEventLinkType = -3l;

}