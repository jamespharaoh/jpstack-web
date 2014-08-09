package wbs.smsapps.ticketer.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketDao;
import wbs.smsapps.ticketer.model.TicketerTicketRec;

public
class TicketerTicketDaoHibernate
	extends HibernateDao
	implements TicketerTicketDao {

	@Override
	public
	TicketerTicketRec findByTicket (
			TicketerRec ticketer,
			NumberRec number,
			String ticket) {

		return findOne (
			TicketerTicketRec.class,

			createQuery (
				"FROM TicketerTicketRec ticketerTicket" +
				"WHERE ticketerTicket.ticketer = :ticketer " +
					"AND ticketerTicket.number = :number " +
					"AND ticketerTicket.ticket = :ticket")

			.setEntity (
				"ticketer",
				ticketer)

			.setEntity (
				"number",
				number)

			.setString (
				"ticket",
				ticket)

			.list ());

	}

}
