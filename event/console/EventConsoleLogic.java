package wbs.platform.event.console;

import javax.inject.Provider;

import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.framework.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;
import wbs.platform.event.model.EventRec;

public
interface EventConsoleLogic {

	PagePart makeEventsPart (
			PermanentRecord<?> object);

	Provider<PagePart> makeEventsPartFactory (
			ObjectLookup<?> objectLookup);

	String eventText (
			EventRec event);

	String objectToHtml (
			Object object);

	void renderEventsTable (
			FormatWriter htmlWriter,
			Iterable<EventRec> events);

	void renderEventRow (
			FormatWriter htmlWriter,
			EventRec event);

}