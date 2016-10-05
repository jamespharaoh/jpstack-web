package wbs.apn.chat.bill.hibernate;

import static wbs.utils.etc.Misc.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.bill.model.ChatRebillLogDaoMethods;
import wbs.apn.chat.bill.model.ChatRebillLogRec;
import wbs.apn.chat.bill.model.ChatRebillLogSearch;
import wbs.framework.hibernate.HibernateDao;

public
class ChatRebillLogDaoHibernate
	extends HibernateDao
	implements ChatRebillLogDaoMethods {

	@Override
	public
	List <Long> searchIds (
			@NonNull ChatRebillLogSearch search) {

		Criteria criteria =
			createCriteria (
				ChatRebillLogRec.class,
				"_chatRebillLog");
	
		// timestamp

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatRebillLog.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatRebillLog.timestamp",
					search.timestamp ().end ()));

		}

		// user

		if (
			isNotNull (
				search.userId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatRebillLog.user.id",
					search.userId ()));

		}

		// last action

		if (
			isNotNull (
				search.lastAction ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatRebillLog.lastAction",
					search.lastAction ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatRebillLog.lastAction",
					search.lastAction ().end ()));

		}

		// minimum credit owed

		if (
			isNotNull (
				search.minimumCreditOwed ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatRebillLog.minimumCreditOwed",
					search.minimumCreditOwed ().getMinimum ()));

			criteria.add (
				Restrictions.lt (
					"_chatRebillLog.minimumCreditOwed",
					search.minimumCreditOwed ().getMaximum ()));

		}

		// user

		if (
			isNotNull (
				search.includeBlocked ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatRebillLog.includeBlocked",
					search.includeBlocked ()));

		}

		// user

		if (
			isNotNull (
				search.includeFailed ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatRebillLog.includeFailed",
					search.includeFailed ()));

		}

		// minimum credit owed

		if (
			isNotNull (
				search.numChatUsers ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatRebillLog.numChatUsers",
					search.numChatUsers ().getMinimum ()));

			criteria.add (
				Restrictions.lt (
					"_chatRebillLog.numChatUsers",
					search.numChatUsers ().getMaximum ()));

		}

		// order newest first

		criteria.addOrder (
			Order.desc (
				"_chatRebillLog.timestamp"));

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// return

		return findMany (
			"searchIds (chatRebillLogSearch)",
			Long.class,
			criteria);
	
	}

}
