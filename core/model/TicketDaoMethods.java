package wbs.services.ticket.core.model;

import java.util.List;

public
interface TicketDaoMethods {

	TicketFieldValueRec findTicketFieldValue (
			TicketRec ticket,
			TicketFieldTypeRec ticketFieldType);

	List<TicketRec> findUnqueuedTickets ();

}