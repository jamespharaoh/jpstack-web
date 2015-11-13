package wbs.services.ticket.core.model;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.TransientObjectException;

import wbs.framework.database.Database;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;

public
class TicketHooks
	extends AbstractObjectHooks<TicketRec> {

	@Inject
	Provider<TicketObjectHelper> ticketHelper;

	@Inject
	Provider<TicketFieldTypeObjectHelper> ticketFieldTypeHelper;

	@Inject
	Provider<TicketFieldValueObjectHelper> ticketFieldValueHelper;

	@Inject
	Provider<ObjectManager> objectManager;

	@Inject
	Provider<QueueLogic> queueLogic;

	@Inject
	Database database;

	@Inject
	RandomLogic randomLogic;

	@Override
	public
	void afterInsert (
			TicketRec ticket) {

		// TODO does not belong here

		if (ticket.getTicketState ().getShowInQueue ()) {

			// create queue item

			QueueItemRec queueItem =
				queueLogic.get ().createQueueItem (
					queueLogic.get ().findQueue (
						ticket.getTicketState (),
						"default"),
					ticket,
					ticket,
					ticket.getCode (),
					ticket.getTicketState ().toString ());

			// add queue item to ticket

			ticket

				.setQueueItem (
					queueItem);

		}

	}

	@Override
	public
	Object getDynamic (
			TicketRec ticket,
			String name) {

		// find the ticket field type

		TicketFieldTypeRec ticketFieldType =
			ticketFieldTypeHelper.get ().findByCode (
				ticket.getTicketManager (),
				name);

		try {

			// find the ticket field value

			TicketFieldValueRec ticketFieldValue =
				ticket.getTicketFieldValues ().get (
					ticketFieldType.getId ());

			if (ticketFieldValue == null) {
				return null;
			}

			switch (ticketFieldType.getDataType ()) {

			case string:

				return ticketFieldValue.getStringValue ();

			case number:

				return ticketFieldValue.getIntegerValue ();

			case bool:

				return ticketFieldValue.getBooleanValue ();

			case object:

				ObjectTypeRec objectType =
					ticketFieldType.getObjectType ();

				Integer objectId =
					ticketFieldValue.getIntegerValue ();

				ObjectHelper<?> objectHelper =
					objectManager.get ().objectHelperForTypeId (
						objectType.getId ());

				Object object =
					objectHelper.find (
						objectId);

				return object;

			default:

				throw new RuntimeException ();

			}

		} catch (TransientObjectException exception) {

			// object not yet saved so fields will all be null

			return null;

		}

	}

	@Override
	public
	void setDynamic (
			TicketRec ticket,
			String name,
			Object value) {

		// find the ticket field type

		TicketFieldTypeRec ticketFieldType =
			ticketFieldTypeHelper.get ().findByCode (
				ticket.getTicketManager (),
				name);

		TicketFieldValueRec ticketFieldValue;

		try {

			 ticketFieldValue =
				ticket.getTicketFieldValues ().get (
					ticketFieldType.getId ());

		} catch (Exception exception) {

			ticketFieldValue =
				null;

		}

		// if the value object does not exist, a new one is created

		if (ticketFieldValue == null) {

			ticketFieldValue =
				ticketFieldValueHelper.get ().createInstance ()

				.setTicket (
					ticket)

				.setTicketFieldType (
					ticketFieldType);

		}

		switch (ticketFieldType.getDataType ()) {

		case string:

			ticketFieldValue

				.setStringValue (
					(String)
					value);

			break;

		case number:

			ticketFieldValue

				.setIntegerValue (
					(Integer)
					value);

			break;

		case bool:

			ticketFieldValue

				.setBooleanValue (
					(Boolean)
					value);

			break;

		case object:

			Record<?> record =
				(Record<?>) value;

			ticketFieldValue.setIntegerValue (
				record.getId ());

			break;

		default:

			throw new RuntimeException ();

		}

		ticket

			.setNumFields (
				ticket.getNumFields () + 1);

		ticket.getTicketFieldValues ().put (
			ticketFieldType.getId (),
			ticketFieldValue);

	}

}