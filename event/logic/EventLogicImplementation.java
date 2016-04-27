package wbs.platform.event.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.PermanentRecord;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventObjectHelper;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeObjectHelper;
import wbs.platform.event.model.EventTypeRec;
import wbs.platform.text.model.TextObjectHelper;

@SingletonComponent ("eventLogic")
public
class EventLogicImplementation
	implements EventLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventObjectHelper eventHelper;

	@Inject
	EventLinkObjectHelper eventLinkHelper;

	@Inject
	EventTypeObjectHelper eventTypeHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	EventRec createEvent (
			@NonNull String typeCode) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup type

		EventTypeRec eventType =
			eventTypeHelper.findByCode (
				GlobalId.root,
				typeCode);

		if (eventType == null) {

			throw new RuntimeException (
				stringFormat (
					"EventType not found: %s",
					typeCode));

		}

		// create event

		EventRec event =
			eventHelper.insert (
				eventHelper.createInstance ()

			.setEventType (
				eventType)

			.setTimestamp (
				transaction.now ())

		);

		// return

		return event;

	}

	@Override
	public
	EventRec createEvent (
			@NonNull String typeCode,
			@NonNull Object... linkObjects) {

		// create event

		EventRec event =
			createEvent (
				typeCode);

		// create event links

		int index = 0;

		for (
			Object linkObject
				: linkObjects
		) {

			EventLinkRec eventLink =
				createEventLink (
					event,
					linkObject,
					index);

			eventLinkHelper.insert (
				eventLink);

			event.getEventLinks ().add (
				eventLink);

			index ++;

		}

		// return

		return event;

	}

	EventLinkRec createEventLink (
			@NonNull EventRec event,
			@NonNull Object linkObject,
			int index) {

		linkObject =
			normaliseLinkObject (
				linkObject,
				index);

		// handle permanent records as object ids

		if (linkObject instanceof PermanentRecord<?>) {

			PermanentRecord<?> dataObject =
				(PermanentRecord<?>) linkObject;

			if (dataObject.getId () == null)
				throw new IllegalArgumentException ();

			return eventLinkHelper.createInstance ()

				.setEvent (
					event)

				.setIndex (
					index)

				.setTypeId (
					(long) (int)
					objectManager.getObjectTypeId (
						dataObject))

				.setRefId (
					(long) dataObject.getId ());

		}

		// store longs directly

		if (linkObject instanceof Long) {

			return eventLinkHelper.createInstance ()

				.setEvent (
					event)

				.setIndex (
					index)

				.setTypeId (
					EventLogic.integerEventLinkType)

				.setRefId (
					(Long) linkObject);

		}

		// store booleans directly

		if (linkObject instanceof Boolean) {

			return eventLinkHelper.createInstance ()

				.setEvent (
					event)

				.setIndex (
					index)

				.setTypeId (
					EventLogic.booleanEventLinkType)

				.setRefId (
					(Boolean) linkObject
						? 1L
						: 0L);

		}

		// store instants directly

		if (linkObject instanceof Instant) {

			Instant instant =
				(Instant)
				linkObject;

			return eventLinkHelper.createInstance ()

				.setEvent (
					event)

				.setIndex (
					index)

				.setTypeId (
					EventLogic.instantEventLinkType)

				.setRefId (
					instant.getMillis ());

		}

		// error otherwise

		throw new RuntimeException (
			stringFormat (
				"Unlinkable type %s passed as link parameter %s",
				linkObject.getClass (),
				index));

	}

	Object normaliseLinkObject (
			@NonNull Object linkObject,
			int index) {

		// don't allow null

		if (linkObject == null) {

			throw new RuntimeException (
				stringFormat (
					"Null passed as link parameter %s",
					index));

		}

		// convert to string, continue processing

		if (
			linkObject instanceof Float
			|| linkObject instanceof Double
			|| linkObject instanceof Enum<?>
			|| linkObject instanceof LocalDate
		) {

			linkObject =
				linkObject.toString ();

		}

		// convert to long, continue processing

		if (linkObject instanceof Integer) {

			linkObject =
				(long) (Integer)
				linkObject;

		}

		// handle strings as text objects

		if (linkObject instanceof String) {

			return textHelper.findOrCreate (
				(String) linkObject);

		}

		// return object unchanged

		return linkObject;

	}

}
