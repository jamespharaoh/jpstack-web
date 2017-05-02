package wbs.platform.event.console;

import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.model.EventRec;

import wbs.utils.string.FormatWriter;

public
interface EventConsoleLogic {

	PagePart makeEventsPart (
			Transaction parentTransaction,
			PermanentRecord <?> object);

	PagePartFactory makeEventsPartFactory (
			TaskLogger parentTaskLogger,
			ObjectLookup <?> objectLookup);

	void writeEventHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			EventRec event);

	void writeObjectAsHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Object object);

	void writeEventsTable (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			Iterable <EventRec> events);

	void writeEventRow (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			EventRec event);

}