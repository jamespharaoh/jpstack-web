package wbs.ticket.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.ticket.model.TicketDao;
import wbs.ticket.model.TicketFieldTypeRec;
import wbs.ticket.model.TicketFieldValueRec;
import wbs.ticket.model.TicketRec;

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

}
