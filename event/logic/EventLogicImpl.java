package wbs.platform.event.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
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
class EventLogicImpl
	implements EventLogic {

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

	@Override
	public
	EventRec createEvent (
			String typeCode) {

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
				new EventRec ()
					.setEventType (eventType)
					.setTimestamp (new Date ()));

		// return

		return event;

	}

	@Override
	public
	EventRec createEvent (
			String typeCode,
			Object... linkObjects) {

		// create event

		EventRec event =
			createEvent (typeCode);

		// create event links

		int index = 0;

		for (Object linkObject
				: linkObjects) {

			createEventLink (
				event,
				linkObject,
				index);

			index ++;

		}

		// return

		return event;

	}

	void createEventLink (
			EventRec event,
			Object linkObject,
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

			event.getEventLinks ().put (
				index,
				eventLinkHelper.insert (
					new EventLinkRec ()
						.setEvent (event)
						.setIndex (index)
						.setTypeId (objectManager.getObjectTypeId (dataObject))
						.setRefId (dataObject.getId ())));

			return;

		}

		// store integers directly

		if (linkObject instanceof Integer) {

			event.getEventLinks ().put (
				index,
				eventLinkHelper.insert (
					new EventLinkRec ()
						.setEvent (event)
						.setIndex (index)
						.setTypeId (-1)
						.setRefId ((Integer) linkObject)));

			return;

		}

		// store booleans directly

		if (linkObject instanceof Boolean) {

			event.getEventLinks ().put (
				index,
				eventLinkHelper.insert (
					new EventLinkRec ()
						.setEvent (event)
						.setIndex (index)
						.setTypeId (-2)
						.setRefId (
							(Boolean) linkObject ? 1 : 0)));

			return;

		}

		// error otherwise

		throw new RuntimeException (
			stringFormat (
				"Unlinkable type %s passed as link parameter %s",
				linkObject.getClass (),
				index));

	}

	Object normaliseLinkObject (
			Object linkObject,
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
		) {

			linkObject =
				linkObject.toString ();

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
