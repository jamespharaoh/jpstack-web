package wbs.platform.event.logic;

import wbs.platform.event.model.EventRec;

public
interface EventLogic {

	EventRec createEvent (
			String typeCode);

	EventRec createEvent (
			String typeCode,
			Object... objects);

}