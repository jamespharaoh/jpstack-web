package wbs.apn.chat.user.core.hibernate;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.category.model.ChatCategoryRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

public
class ChatUserDaoHibernate
	extends HibernateDao
	implements ChatUserDao {

	@Override
	public
	ChatUserRec find (
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"find (chat, number)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.add (
				Restrictions.eq (
					"_chatUser.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatUser.number",
					number))

		);

	}

	@Override
	public
	Long countOnline (
			@NonNull ChatRec chat,
			@NonNull ChatUserType type) {

		return findOneOrNull (
			"countOnline (chat, type)",
			Long.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.add (
				Restrictions.eq (
					"_chatUser.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatUser.type",
					type))

			.add (
				Restrictions.eq (
					"_chatUser.online",
					true))

			.setProjection (
				Projections.rowCount ())

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingBill (
			@NonNull ChatRec chat,
			@NonNull Instant lastAction,
			@NonNull Long maximumCredit) {

		return findMany (
			"findWantingBill",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"chat",
					chat))

			.add (
				Restrictions.eq (
					"type",
					ChatUserType.user))

			.add (
				Restrictions.lt (
					"credit",
					maximumCredit))

			.add (
				Restrictions.eq (
					"creditMode",
					ChatUserCreditMode.billedMessages))

			.add (
				Restrictions.ge (
					"lastAction",
					lastAction))

/*
			.add (
				Restrictions.sqlRestriction (
					"credit + credit_revoked < 0"))
*/

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingWarning () {

		return findMany (
			"findWantingWarning",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.createAlias (
				"_chatUser.chatScheme",
				"_chatScheme")

			.createAlias (
				"_chatScheme.charges",
				"_chatSchemeCharges")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.isNotNull (
					"_chatSchemeCharges.spendWarningEvery"))

			.add (
				Restrictions.eq (
					"_chatUser.type",
					ChatUserType.user))

			.add (
				Restrictions.geProperty (
					"_chatUser.valueSinceWarning",
					"_chatSchemeCharges.spendWarningEvery"))

		);

	}

	@Override
	public
	List <ChatUserRec> findAdultExpiryLimit (
			@NonNull Instant now,
			int maxResults) {

		return findMany (
			"findAdultExpiryLimit",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.lt (
					"_chatUser.adultExpiry",
					now))

			.setMaxResults (
				maxResults)

		);

	}

	@Override
	public
	List <ChatUserRec> findOnline (
			@NonNull ChatUserType type) {

		return findMany (
			"findOnline",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"online",
					true))

			.add (
				Restrictions.eq (
					"type",
					type))

			.addOrder (
				Order.desc (
					"lastAction"))

		);

	}

	@Override
	public
	List <ChatUserRec> findOnlineOrMonitorCategory (
			@NonNull ChatRec chat,
			@NonNull ChatCategoryRec category) {

		Criteria criteria =
			createCriteria (
				ChatUserRec.class,
				"_chatUser");

		criteria.add (
			Restrictions.eq (
				"_chatUser.chat",
				chat));

		criteria.add (
			Restrictions.or (
				Restrictions.eq (
					"_chatUser.online",
					true),
				Restrictions.and (
					Restrictions.eq (
						"_chatUser.type",
						ChatUserType.monitor),
					Restrictions.eq (
						"_chatUser.category",
						category))));

		criteria.addOrder (
			Order.desc (
				"_chatUser.lastAction"));

		return findMany (
			"findOnlineOrMonitorCategory",
			ChatUserRec.class,
			criteria);

	}

	@Override
	public
	List <ChatUserRec> find (
			@NonNull ChatRec chat,
			@NonNull ChatUserType type,
			@NonNull Orient orient,
			@NonNull Gender gender) {

		return findMany (
			"find (chat, type, orient, gender)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"chat",
					chat))

			.add (
				Restrictions.eq (
					"type",
					type))

			.add (
				Restrictions.eq (
					"orient",
					orient))

			.add (
				Restrictions.eq (
					"gender",
					gender))

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingJoinOutbound (
			@NonNull Instant now) {

		return findMany (
			"findWantingJoinOutbound (now)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.le (
					"_chatUser.nextJoinOutbound",
					now))

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingAdultAd (
			@NonNull Instant now) {

		return findMany (
			"findWantingAdultAdd (now)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.lt (
					"_chatUser.nextAdultAd",
					now))

		);

	}

	@Override
	public
	List <ChatUserRec> findOnline (
			@NonNull ChatRec chat) {

		return findMany (
			"findOnline (chat)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"chat",
					chat))

			.add (
				Restrictions.eq (
					"online",
					true))

			.addOrder (
				Order.desc (
					"lastAction"))

		);

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserSearch search) {

		Criteria criteria =

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.createAlias (
				"_chat.slice",
				"_slice")

			.createAlias (
				"_chatUser.number",
				"_number",
				JoinType.LEFT_OUTER_JOIN);

		if (
			isNotNull (
				search.chatId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chat.id",
					search.chatId ()));

		}

		if (
			isNotNull (
				search.type ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.type",
					search.type ()));

		}

		if (
			isNotNull (
				search.typeIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.type",
					search.typeIn ()));

		}

		if (
			isNotNull (
				search.code ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.code",
					search.code ()));

		}

		if (
			isNotNull (
				search.codeIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.code",
					search.codeIn ()));

		}

		if (
			isNotNull (
				search.blockAll ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.blockAll",
					search.blockAll ()));

		}

		if (
			isNotNull (
				search.barred ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.barred",
					search.barred ()));

		}

		if (
			isNotNull (
				search.deleted ())
		) {

			criteria.add (
				search.deleted ()

				? Restrictions.isNull (
					"_chatUser.number")

				: Restrictions.or (

					Restrictions.eq (
						"_chatUser.type",
						ChatUserType.monitor),

					Restrictions.isNotNull (
						"_chatUser.number"))

			);

		}

		if (
			isNotNull (
				search.hasGender ())
		) {

			if (search.hasGender ()) {

				criteria.add (
					Restrictions.isNotNull (
						"_chatUser.gender"));

			} else {

				criteria.add (
					Restrictions.isNull (
						"_chatUser.gender"));

			}

		}

		if (
			isNotNull (
				search.gender ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.gender",
					search.gender ()));

		}

		if (
			isNotNull (
				search.genderIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.genderIn",
					search.genderIn ()));

		}

		if (
			isNotNull (
				search.hasOrient ())
		) {

			if (search.hasOrient ()) {

				criteria.add (
					Restrictions.isNotNull (
						"_chatUser.orient"));

			} else {

				criteria.add (
					Restrictions.isNull (
						"_chatUser.orient"));

			}

		}

		if (
			isNotNull (
				search.orient ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.orient",
					search.orient ()));

		}

		if (
			isNotNull (
				search.orientIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.orientIn",
					search.orientIn ()));

		}

		if (
			isNotNull (
				search.chatAffiliateId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.chatAffiliate.id",
					search.chatAffiliateId ()));

		}

		if (
			isNotNull (
				search.hasCategory ())
		) {

			criteria.add (
				search.hasCategory ()

				? Restrictions.isNotNull (
					"_chatUser.category")

				: Restrictions.isNull (
					"_chatUser.category"));

		}

		if (
			isNotNull (
				search.categoryId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.category.id",
					search.categoryId ()));

		}

		if (
			isNotNull (
				search.numberId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.number.id",
					search.numberId ()));

		}

		if (
			isNotNull (
				search.numberLike ())
		) {

			criteria.add (
				Restrictions.like (
					"_number.number",
					search.numberLike ()));

		}

		if (
			isNotNull (
				search.name ())
		) {

			criteria.add (
				Restrictions.ilike (
					"_chatUser.name",
					search.name ()));

		}

		if (
			isNotNull (
				search.info ())
		) {

			criteria.createAlias (
				"_chatUser.infoText",
				"_infoText",
				JoinType.LEFT_OUTER_JOIN);

			criteria.add (
				Restrictions.ilike (
					"_infoText.text",
					search.info ()));

		}

		if (
			isNotNull (
				search.location ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatuser.locationPlace",
					search.location ()
			).ignoreCase ());

		}

		if (
			isNotNull (
				search.online ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.online",
					search.online ()));

		}

		if (
			isNotNull (
				search.hasPicture ())
		) {

			criteria.add (
				search.hasPicture ()

				? Restrictions.isNotNull (
					"_chatUser.mainChatUserImage")

				: Restrictions.isNull (
					"_chatUser.mainChatUserImage")

			);

		}

		if (
			isNotNull (
				search.hasVideo ())
		) {

			criteria.add (
				search.hasVideo ()

				? Restrictions.isNotNull (
					"_chatUser.mainChatUserVideo")

				: Restrictions.isNull (
					"_chatUser.mainChatUserVideo")

			);

		}

		if (
			isNotNull (
				search.hasAudio ())
		) {

			criteria.add (
				search.hasAudio ()

				? Restrictions.isNotNull (
					"_chatUser.mainChatUserAudio")

				: Restrictions.isNull (
					"_chatUser.mainChatUserAudio")

			);

		}

		if (
			isNotNull (
				search.adultVerified ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.adultVerified",
					search.adultVerified ()));

		}

		if (
			isNotNull (
				search.creditMode ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.creditMode",
					search.creditMode ()));

		}

		if (
			isNotNull (
				search.creditFailed ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.creditFailed",
					search.creditFailed ().getMinimum ()));

			criteria.add (
				Restrictions.le (
					"_chatUser.creditFailed",
					search.creditFailed ().getMaximum ()));

		}

		if (
			isNotNull (
				search.creditNoReports ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.creditSent",
					search.creditNoReports ().getMinimum ()));

			criteria.add (
				Restrictions.le (
					"_chatUser.creditSent",
					search.creditNoReports ().getMaximum ()));

		}

		if (
			isNotNull (
				search.valueSinceEver ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.valueSinceEver",
					search.valueSinceEver ().getMinimum ()));

			criteria.add (
				Restrictions.le (
					"_chatUser.valueSinceEver",
					search.valueSinceEver ().getMaximum ()));

		}

		if (
			isNotNull (
				search.firstJoin ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.firstJoin",
					search.firstJoin ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatUser.firstJoin",
					search.firstJoin ().end ()));

		}

		if (
			isNotNull (
				search.lastAction ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.lastAction",
					search.lastAction ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatUser.lastAction",
					search.lastAction ().end ()));

		}

		if (
			isNotNull (
				search.lastJoin ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.lastJoin",
					search.lastJoin ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatUser.lastJoin",
					search.lastJoin ().end ()));

		}

		if (
			isNotNull (
				search.datingMode ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.dateMode",
					search.datingMode ()));

		}

		if (
			isNotNull (
				search.hasDatingMode ())
		) {

			criteria.add (
				search.hasDatingMode ()

				? Restrictions.isNotNull (
					"_chatUser.dateMode")

				: Restrictions.isNull (
					"_chatUser.dateMode")

			);

		}

		if (
			isNotNull (
				search.deliveryMethodIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.deliveryMethod",
					search.deliveryMethodIn ()));

		}

		switch (search.order ()) {

		case code:

			criteria
				.addOrder (Order.asc ("_slice.code"))
				.addOrder (Order.asc ("_chat.code"))
				.addOrder (Order.asc ("_chatUser.code"));

			break;

		default:

			shouldNeverHappen ();

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			"searchIds (search)",
			Long.class,
			criteria);

	}

	@Override
	public
	List <ChatUserRec> find (
			@NonNull ChatAffiliateRec chatAffiliate) {

		return findMany (
			"find (chatAffiliate)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"chatAffiliate",
					chatAffiliate))

		);

	}

	@Override
	public
	List <ChatUserRec> findDating (
			@NonNull ChatRec chat) {

		return findMany (
			"findDating (chat)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class)

			.add (
				Restrictions.eq (
					"chat",
					chat))

			.add (
				Restrictions.eq (
					"dateMode",
					ChatUserDateMode.none))

			.add (
				Restrictions.isNotNull (
					"gender"))

			.add (
				Restrictions.isNotNull (
					"orient"))

			.add (
				Restrictions.isNotNull (
					"locationLongLat"))

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingAd (
			@NonNull Instant now) {

		return findMany (
			"findWantingAd (now)",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.lt (
					"_chatUser.nextAd",
					now))

		);

	}

	@Override
	public
	List <ChatUserRec> findWantingQuietOutbound (
			@NonNull Instant now) {

		return findMany (
			"findWantingQuietOutbound",
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.le (
					"_chatUser.nextQuietOutbound",
					now))

		);

	}

}
