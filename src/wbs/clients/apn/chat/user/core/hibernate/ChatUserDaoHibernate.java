package wbs.clients.apn.chat.user.core.hibernate;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.bill.hibernate.ChatUserCreditModeType;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.category.model.ChatCategoryRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.clients.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;

public
class ChatUserDaoHibernate
	extends HibernateDao
	implements ChatUserDao {

	@Override
	public
	ChatUserRec find (
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		return findOne (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec chatUser " +
				"WHERE chatUser.chat = :chat " +
				"AND chatUser.number = :number")

			.setEntity (
				"chat", chat)

			.setEntity (
				"number",
				number)

			.list ());

	}

	@Override
	public
	int countOnline (
			@NonNull ChatRec chat,
			@NonNull ChatUserType type) {

		return (int) (long) findOne (
			Long.class,

			createQuery (
				"SELECT count (*) " +
				"FROM ChatUserRec cu " +
				"WHERE cu.chat = :chat " +
				"AND cu.type = :type " +
				"AND cu.online = true")

			.setEntity (
				"chat",
				chat)

			.setParameter (
				"type",
				type,
				ChatUserTypeType.INSTANCE)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingBill (
			@NonNull Date date) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.type = :type " +
				"AND cu.credit < 0 " +
				"AND cu.creditMode = :creditModeStrict " +
				"AND cu.lastAction >= :date " +
				"AND cu.credit + cu.creditRevoked < 0")

			.setParameter (
				"type",
				ChatUserType.user,
				ChatUserTypeType.INSTANCE)

			.setParameter (
				"creditModeStrict",
				ChatUserCreditMode.strict,
				ChatUserCreditModeType.INSTANCE)

			.setTimestamp (
				"date",
				date)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingWarning () {

		return findMany (
			ChatUserRec.class,

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chatScheme",
				"_chatScheme")

			.createAlias (
				"_chatScheme.charges",
				"_chatSchemeCharges")

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

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findAdultExpiryLimit (
			@NonNull Instant now,
			int maxResults) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.adultExpiry < :now")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findOnline (
			ChatUserType type) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.online = true " +
					"AND cu.type = :type " +
				"ORDER BY cu.lastAction DESC")

			.setParameter (
				"type",
				type,
				ChatUserTypeType.INSTANCE)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findOnlineOrMonitorCategory (
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
			ChatUserRec.class,
			criteria.list ());

	}

	@Override
	public
	List<ChatUserRec> find (
			ChatRec chat,
			ChatUserType type,
			Orient orient,
			Gender gender) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.chat = :chat " +
				"AND cu.type = :type " +
				"AND cu.orient = :orient " +
				"AND cu.gender = :gender")

			.setEntity (
				"chat",
				chat)

			.setParameter (
				"type",
				type,
				ChatUserTypeType.INSTANCE)

			.setParameter (
				"orient",
				orient,
				OrientType.INSTANCE)

			.setParameter (
				"gender",
				gender,
				GenderType.INSTANCE)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingJoinOutbound (
			Instant now) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextJoinOutbound <= :now")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingAdultAd (
			Instant now) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextAdultAd < :now ")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findOnline (
			ChatRec chat) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.chat = :chat " +
					"AND cu.online = true " +
				"ORDER BY cu.lastAction DESC")

			.setEntity (
				"chat",
				chat)

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			@NonNull Map<String,Object> searchMap) {

		Criteria criteria =
			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.setProjection (
				Projections.id ())

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.createAlias (
				"_chatUser.number",
				"_number",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"_chatUser.oldNumber",
				"_oldNumber",
				JoinType.LEFT_OUTER_JOIN);

		for (
			Map.Entry<String,Object> entry
				: searchMap.entrySet ()
		) {

			String key =
				entry.getKey ();

			Object value =
				entry.getValue ();

			if (key.equals ("code")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.code",
						value));

			} else if (key.equals ("codeIn")) {

				criteria.add (
					Restrictions.in (
						"_chatUser.code",
						(Collection<?>) value));

			} else if (key.equals ("number")) {

				criteria.add (
					Restrictions.ilike (
						"_number.number",
						value));

			} else if (key.equals ("numberId")) {

				criteria.add (
					Restrictions.eq (
						"_number.id",
						value));

			} else if (key.equals ("oldNumber")) {

				criteria.add (
					Restrictions.ilike (
						"_oldNumber.number",
						value));

			} else if (key.equals ("oldNumberId")) {

				criteria.add (
					Restrictions.eq (
						"_oldNumber.id",
						value));

			} else if (key.equals ("notDeleted")) {

				criteria.add (Restrictions.or (
						Restrictions.eq (
							"_chatUser.type",
							ChatUserType.monitor),
						Restrictions.isNotNull (
						"_number.id")));

			} else if (key.equals ("nameILike")) {

				criteria.add (
					Restrictions.ilike (
						"_chatUser.name",
						value));

			} else if (key.equals ("infoILike")) {

				criteria.createAlias (
					"infoText",
					"_infoText",
					JoinType.LEFT_OUTER_JOIN);

				criteria.add (
					Restrictions.ilike (
						"_infoText.text",
						value));

			} else if (key.equals ("chatId")) {

				criteria.add (
					Restrictions.eq (
						"_chat.id",
						value));

			} else if (key.equals ("type")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.type",
						value));

			} else if (key.equals ("hasImage")) {

				DetachedCriteria hasImageCriteria =
					DetachedCriteria

						.forClass (
							ChatUserImageRec.class,
							"_chatUserImage")

						.add (
							Restrictions.eqProperty (
								"_chatUser.id",
								"_chatUserImage.chatUser.id"))

						.add (
							Restrictions.eq (
								"_chatUserImage.type",
								ChatUserImageType.image))

						.add (
							Restrictions.isNotNull (
								"_chatUserImage.index"))

						.setProjection (
							Property.forName (
								"_chatUserImage.id"));

				if ((Boolean) value) {

					criteria.add (
						Subqueries.exists (
							hasImageCriteria));

				} else {

					criteria.add (
						Subqueries.notExists (
							hasImageCriteria));

				}

			} else if (key.equals ("hasVideo")) {

				DetachedCriteria hasVideoCriteria =
					DetachedCriteria

						.forClass (
							ChatUserImageRec.class,
							"_chatUserVideo")

						.add (
							Restrictions.eqProperty (
								"_chatUserVideo.chatUser.id",
								"_chatUser.id"))

						.add (
							Restrictions.eq (
								"_chatUserVideo.type",
								ChatUserImageType.video))

						.add (
							Restrictions.isNotNull (
								"_chatUserVideo.index"))

						.setProjection (
							Property.forName (
								"_chatUserVideo.id"));

				if ((Boolean) value) {

					criteria.add (
						Subqueries.exists (hasVideoCriteria));

				} else {

					criteria.add (
						Subqueries.notExists (hasVideoCriteria));

				}

			} else if (key.equals ("hasAudio")) {

				DetachedCriteria hasAudioCritieria =
					DetachedCriteria

						.forClass (
							ChatUserImageRec.class,
							"_chatUserAudio")

						.add (
							Restrictions.eqProperty (
								"_chatUser.id",
								"_chatUserAudio.chatUser.id"))

						.add (
							Restrictions.eq (
								"_chatUserAudio.type",
								ChatUserImageType.audio))

						.add (
							Restrictions.isNotNull (
								"_chatUserAudio.index"))

						.setProjection (
							Property.forName (
								"_chatUserAudio.id"));

				if ((Boolean) value) {

					criteria.add (
						Subqueries.exists (hasAudioCritieria));

				} else {

					criteria.add (
						Subqueries.notExists (hasAudioCritieria));

				}

			} else if (key.equals ("adultVerified")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.adultVerified",
						value));

			} else if (key.equals ("chatAffiliateId")) {

				criteria.createAlias (
					"_chatUser.chatAffiliate",
					"_chatAffiliate",
					JoinType.LEFT_OUTER_JOIN);

				criteria.add (
					Restrictions.eq (
						"_chatAffiliate.id",
						value));

			} else if (key.equals ("firstJoinAfter")) {

				criteria.add (
					Restrictions.ge (
						"_chatUser.firstJoin",
						value));

			} else if (key.equals ("firstJoinBefore")) {

				criteria.add (
					Restrictions.lt (
						"_chatUser.firstJoin",
						value));

			} else if (key.equals ("creditMode")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.creditMode",
						value));

			} else if (key.equals ("dateMode")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.dateMode",
						value));

			} else if (key.equals ("online")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.online",
						value));

			} else if (equal (key, "gender")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.gender",
						value));

			} else if (equal (key, "locPlace")) {

				criteria.add (
					Restrictions.eq (
						"_chatuser.locationPlace",
						value));

			} else if (equal (key, "orient")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.orient",
						value));

			} else if (equal (key, "typeIn")) {

				if (((Collection<?>) value).size () == 0)
					return ImmutableList.<Integer>of ();

				criteria.add (
					Restrictions.in (
						"_chatUser.type",
						(Collection<?>) value));

			} else if (key.equals ("hasGender")) {

				criteria.add (
					(Boolean) value
						? Restrictions.isNotNull (
							"_chatUser.gender")
						: Restrictions.isNull (
							"_chatUser.gender"));

			} else if (equal (key, "genderIn")) {

				if (((Collection<?>) value).size () == 0)
					return ImmutableList.<Integer>of ();

				criteria.add (
					Restrictions.in (
						"_chatUser.gender",
						(Collection<?>) value));

			} else if (key.equals ("hasOrient")) {

				criteria.add ((Boolean) value
					? Restrictions.isNotNull (
						"_chatUser.orient")
					: Restrictions.isNull (
						"_chatUser.orient"));

			} else if (equal (key, "orientIn")) {

				if (((Collection<?>) value).size () == 0)
					return Collections.emptyList ();

				criteria.add (
					Restrictions.in (
						"_chatUser.orient",
						(Collection<?>) value));

			} else if (key.equals ("hasDateMode")) {

				criteria.add ((Boolean) value
					? Restrictions.isNotNull (
						"_chatUser.dateMode")
					: Restrictions.isNull (
						"_chatUser.dateMode"));

			} else if (key.equals ("blockAll")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.blockAll",
						value));

			} else if (key.equals ("barred")) {

				criteria.add (
					Restrictions.eq (
						"_chatUser.barred",
						value));

			} else if (equal (key, "creditFailedGte")) {

				criteria.add (
					Restrictions.ge (
						"_chatUser.creditFailed",
						value));

			} else if (equal (key, "creditFailedLte")) {

				criteria.add (
					Restrictions.le (
						"_chatUser.creditFailed",
						value));

			} else if (equal (key, "creditNoReportGte")) {

				criteria.add (
					Restrictions.ge (
						"_chatUser.creditSent",
						value));

			} else if (equal (key, "creditNoReportLte")) {

				criteria.add (
					Restrictions.le (
						"_chatUser.creditSent",
						value));

			} else if (equal (key, "valueSinceEverGte")) {

				criteria.add (
					Restrictions.ge (
						"_chatUser.valueSinceEver",
						value));

			} else if (equal (key, "valueSinceEverLte")) {

				criteria.add (
					Restrictions.le (
						"_chatUser.valueSinceEver",
						value));

			} else if (
				equal (
					key,
					"onlineAfter")
			) {

				Date dateValue =
					instantToDate (
						(Instant)
						value);

				DetachedCriteria onlineAfterCriteria =
					DetachedCriteria

					.forClass (
						ChatUserSessionRec.class,
						"_chatUserSession")

					.add (
						Restrictions.eqProperty (
							"_chatUser.id",
							"_chatUserSession.chatUser.id"))

					.add (
						Restrictions.or (
							Restrictions.ge (
								"_chatUserSession.endTime",
								dateValue),
							Restrictions.isNull (
								"_chatUserSession.endTime")))

					.setProjection (
						Property.forName (
							"_chatUserSession.id"));

				criteria.add (
					Subqueries.exists (
						onlineAfterCriteria));

			} else if (
				equal (
					key,
					"deliveryMethodIn")
			) {

				criteria.add (
					Restrictions.in (
						"_chatUser.deliveryMethod",
						(Collection<?>) value));

			} else if (equal (key, "lastMessagePollBefore")) {

				criteria.add (
					Restrictions.lt (
						"_chatUser.lastMessagePoll",
						value));

			} else if (equal (key, "lastActionAfter")) {

				criteria.add (
					Restrictions.ge (
						"_chatUser.lastAction",
						value));

			} else if (equal (key, "lastActionBefore")) {

				criteria.add (
					Restrictions.lt (
						"_chatUser.lastAction",
						value));

			} else if (key.equals ("orderBy")) {

				if (value.equals ("code")) {

					criteria.addOrder (
						Order.asc (
							"_chatUser.code"));

				} else if (value.equals ("creditFailedDesc")) {

					criteria.addOrder (
						Order.desc (
							"_chatUser.creditFailed"));

				} else {

					throw new IllegalArgumentException (
						stringFormat (
							"Unknown order by: %s",
							 value));

				}

			} else if (equal (key, "limit")) {

				criteria.setMaxResults (
					(int) (long) (Long)
					value);

			} else {

				throw new IllegalArgumentException (
					stringFormat (
						"Unknown search parameter: %s",
						key));

			}

		}

		return findMany (
			Integer.class,
			criteria.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			@NonNull ChatUserSearch search) {

		Criteria criteria =

			createCriteria (
				ChatUserRec.class,
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

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
				search.code ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.code",
					search.code ()));

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
				search.gender ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUser.gender",
					search.gender ()));

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
					search.numberLike ()));

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
						"_chatUser.mainChatUserImage"));

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
						"_chatUser.mainChatUserVideo"));

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
					instantToDate (
						search.firstJoin ().getStart ())));

			criteria.add (
				Restrictions.lt (
					"_chatUser.firstJoin",
					instantToDate (
						search.firstJoin ().getEnd ())));

		}

		if (
			isNotNull (
				search.lastAction ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.lastAction",
					instantToDate (
						search.lastAction ().getStart ())));

			criteria.add (
				Restrictions.lt (
					"_chatUser.lastAction",
					instantToDate (
						search.lastAction ().getEnd ())));

		}

		if (
			isNotNull (
				search.lastJoin ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUser.lastJoin",
					instantToDate (
						search.lastJoin ().getStart ())));

			criteria.add (
				Restrictions.lt (
					"_chatUser.lastJoin",
					instantToDate (
						search.lastJoin ().getEnd ())));

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
				search.deliveryMethodIn ())
		) {

			criteria.add (
				Restrictions.in (
					"_chatUser.deliveryMethod",
					search.deliveryMethodIn ()));

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			criteria.list ());

	}

	@Override
	public
	List<ChatUserRec> find (
			ChatAffiliateRec chatAffiliate) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.chatAffiliate = :chatAffiliate")

			.setEntity (
				"chatAffiliate",
				chatAffiliate)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findDating (
			ChatRec chat) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.dateMode != :dateModeNone " +
					"AND cu.gender IS NOT NULL " +
					"AND cu.orient IS NOT NULL " +
					"AND cu.locationLongLat IS NOT NULL " +
					"AND cu.chat = :chat")

			.setParameter (
				"dateModeNone",
				ChatUserDateMode.none,
				ChatUserDateModeType.INSTANCE)

			.setEntity (
				"chat",
				chat)

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingAd (
			Instant now) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec chatUser " +
				"WHERE chatUser.nextAd < :now ")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingQuietOutbound (
			Instant now) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextQuietOutbound <= :now")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.list ());

	}

}
