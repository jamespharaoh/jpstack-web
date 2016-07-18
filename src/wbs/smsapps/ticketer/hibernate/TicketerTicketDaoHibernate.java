package wbs.smsapps.ticketer.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull TicketerRec ticketer,
			@NonNull NumberRec number,
			@NonNull String ticket) {

		return findOne (
			"findByTicket (ticketer, number, ticket)",
			TicketerTicketRec.class,

			createCriteria (
				TicketerTicketRec.class,
				"_ticketerTicket")

			.add (
				Restrictions.eq (
					"_ticketerTicket.ticketer",
					ticketer))

			.add (
				Restrictions.eq (
					"_ticketerTicket.number",
					number))

			.add (
				Restrictions.eq (
					"_ticketerTicket.ticket",
					ticket))

		);

	}

}
