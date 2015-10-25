package wbs.platform.event.console;

import javax.inject.Provider;

import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.framework.record.PermanentRecord;
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

}