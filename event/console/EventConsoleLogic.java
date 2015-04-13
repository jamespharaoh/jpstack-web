package wbs.platform.event.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;

@SingletonComponent ("eventConsoleLogic")
public
class EventConsoleLogic {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	Provider<ObjectEventsPart> objectEventsPart;

	public
	PagePart makeEventsPart (
			PermanentRecord<?> object) {

		List<Record<?>> children =
			objectManager.getMinorChildren (
				object);

		List<GlobalId> objectGlobalIds =
			new ArrayList<GlobalId> ();

		objectGlobalIds.add (
			objectManager.getGlobalId (object));

		for (Record<?> child : children)
			objectGlobalIds.add (
				objectManager.getGlobalId (child));

		return objectEventsPart.get ()
			.dataObjectIds (objectGlobalIds);

	}

	// ================================= make events part factory

	public
	Provider<PagePart> makeEventsPartFactory (
			final ObjectLookup<?> objectLookup) {

		return new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				@Cleanup
				Transaction transaction =
					database.beginReadOnly (
						this);

				PermanentRecord<?> object =
					(PermanentRecord<?>)
					objectLookup.lookupObject (
						requestContext.contextStuff ());

				return makeEventsPart (
					object);

			}

		};

	}

}
