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
	int integerEventLinkType = -1;

	public final static
	int booleanEventLinkType = -2;

	public final static
	int instantEventLinkType = -3;

}