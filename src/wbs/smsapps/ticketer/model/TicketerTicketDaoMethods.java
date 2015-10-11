package wbs.smsapps.ticketer.model;

import wbs.sms.number.core.model.NumberRec;

public
interface TicketerTicketDaoMethods {

	TicketerTicketRec findByTicket (
			TicketerRec ticketer,
			NumberRec number,
			String ticket);

}