package wbs.platform.event.console;

import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.model.EventRec;

import wbs.utils.string.FormatWriter;

public
interface EventConsoleLogic {

	PagePart makeEventsPart (
			PermanentRecord <?> object);

	PagePartFactory makeEventsPartFactory (
			ObjectLookup <?> objectLookup);

	void writeEventHtml (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			EventRec event);

	void writeObjectAsHtml (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			Object object);

	void writeEventsTable (
			TaskLogger taskLogger,
			FormatWriter htmlWriter,
			Iterable <EventRec> events);

	void writeEventRow (
			TaskLogger taskLogger,
			FormatWriter htmlWriter,
			EventRec event);

}