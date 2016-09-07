package wbs.services.ticket.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.services.ticket.core.model.TicketDao;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketRec;

@SingletonComponent ("ticketDaoHibernate")
public
class TicketDaoHibernate
	extends HibernateDao
	implements TicketDao {

	@Override
	public
	TicketFieldValueRec findTicketFieldValue (
			@NonNull TicketRec ticket,
			@NonNull TicketFieldTypeRec ticketFieldType) {

		return findOne (
			"findTicketFieldValue (ticket, ticketFieldType)",
			TicketFieldValueRec.class,

			createCriteria (
				TicketFieldValueRec.class,
				"_ticketFieldValue")

			.add (
				Restrictions.eq (
					"_ticketFieldValue.ticket",
					ticket))

			.add (
				Restrictions.eq (
					"_ticketFieldValue.ticketFieldType",
					ticketFieldType))

		);

	}

	@Override
	public
	List<TicketRec> findUnqueuedTickets () {

		return findMany (
			"findUnqueuedTickets ()",
			TicketRec.class,

			createCriteria (
				TicketRec.class,
				"_ticket")

			.add (
				Restrictions.eq (
					"_ticket.queued",
					false))

		);

	}

}
