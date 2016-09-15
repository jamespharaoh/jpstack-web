package wbs.platform.event.console;

import javax.inject.Provider;

import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.framework.entity.record.PermanentRecord;
import wbs.platform.event.model.EventRec;
import wbs.utils.string.FormatWriter;

public
interface EventConsoleLogic {

	PagePart makeEventsPart (
			PermanentRecord <?> object);

	Provider <PagePart> makeEventsPartFactory (
			ObjectLookup <?> objectLookup);

	void writeEventHtml (
			FormatWriter formatWriter,
			EventRec event);

	void writeObjectAsHtml (
			FormatWriter formatWriter,
			Object object);

	void writeEventsTable (
			FormatWriter htmlWriter,
			Iterable <EventRec> events);

	void writeEventRow (
			FormatWriter htmlWriter,
			EventRec event);

}