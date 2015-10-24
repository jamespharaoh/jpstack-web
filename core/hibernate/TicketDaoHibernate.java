package wbs.services.ticket.core.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.framework.application.annotations.SingletonComponent;
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
			TicketRec ticket,
			TicketFieldTypeRec ticketFieldType) {

		return findOne (
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

			.list ());

	}

	@Override
	public
	List<TicketRec> findUnqueuedTickets () {

		return findMany (
				TicketRec.class,

				createQuery (
					"FROM TicketRec" +
					" WHERE queued = 'false'")

				.list ());

		}

}
