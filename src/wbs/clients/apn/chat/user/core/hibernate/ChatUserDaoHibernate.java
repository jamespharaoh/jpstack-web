package wbs.clients.apn.chat.user.core.hibernate;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.bill.hibernate.ChatUserCreditModeType;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;

import com.google.common.collect.ImmutableList;

public
class ChatUserDaoHibernate
	extends HibernateDao
	implements ChatUserDao {

	@Override
	public
	ChatUserRec find (
			ChatRec chat,
			NumberRec number) {

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
			ChatRec chat,
			ChatUserType type) {

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
			Date date) {

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
			int maxResults) {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.adultExpiry < :now")

			.setTimestamp (
				"now",
				new Date ())

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
	List<ChatUserRec> findWantingJoinOutbound () {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextJoinOutbound <= :now")

			.setTimestamp (
				"now",
				new Date ())

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingAdultAd () {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextAdultAd < :now ")

			.setTimestamp (
				"now",
				new Date ())

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
			Map<String,Object> searchMap) {

		Criteria crit =
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

		for (Map.Entry<String,Object> entry
				: searchMap.entrySet ()) {

			String key =
				entry.getKey ();

			Object value =
				entry.getValue ();

			if (key.equals ("code")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.code",
						value));

			} else if (key.equals ("codeIn")) {

				crit.add (
					Restrictions.in (
						"_chatUser.code",
						(Collection<?>) value));

			} else if (key.equals ("number")) {

				crit.add (
					Restrictions.ilike (
						"_number.number",
						value));

			} else if (key.equals ("numberId")) {

				crit.add (
					Restrictions.eq (
						"_number.id",
						value));

			} else if (key.equals ("oldNumber")) {

				crit.add (
					Restrictions.ilike (
						"_oldNumber.number",
						value));

			} else if (key.equals ("oldNumberId")) {

				crit.add (
					Restrictions.eq (
						"_oldNumber.id",
						value));

			} else if (key.equals ("notDeleted")) {

				crit.add (Restrictions.or (
						Restrictions.eq (
							"_chatUser.type",
							ChatUserType.monitor),
						Restrictions.isNotNull (
						"_number.id")));

			} else if (key.equals ("nameILike")) {

				crit.add (
					Restrictions.ilike (
						"_chatUser.name",
						value));

			} else if (key.equals ("infoILike")) {

				crit.createAlias (
					"infoText",
					"_infoText");

				crit.add (
					Restrictions.ilike (
						"_infoText.text",
						value));

			} else if (key.equals ("chatId")) {

				crit.add (
					Restrictions.eq (
						"_chat.id",
						value));

			} else if (key.equals ("type")) {

				crit.add (
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

					crit.add (
						Subqueries.exists (
							hasImageCriteria));

				} else {

					crit.add (
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

					crit.add (
						Subqueries.exists (hasVideoCriteria));

				} else {

					crit.add (
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

					crit.add (
						Subqueries.exists (hasAudioCritieria));

				} else {

					crit.add (
						Subqueries.notExists (hasAudioCritieria));

				}

			} else if (key.equals ("adultVerified")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.adultVerified",
						value));

			} else if (key.equals ("chatAffiliateId")) {

				crit.createAlias (
					"_chatUser.chatAffiliate",
					"_chatAffiliate");

				crit.add (
					Restrictions.eq (
						"_chatAffiliate.id",
						value));

			} else if (key.equals ("firstJoinAfter")) {

				crit.add (
					Restrictions.ge (
						"_chatUser.firstJoin",
						value));

			} else if (key.equals ("firstJoinBefore")) {

				crit.add (
					Restrictions.lt (
						"_chatUser.firstJoin",
						value));

			} else if (key.equals ("creditMode")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.creditMode",
						value));

			} else if (key.equals ("dateMode")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.dateMode",
						value));

			} else if (key.equals ("online")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.online",
						value));

			} else if (equal (key, "gender")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.gender",
						value));

			} else if (equal (key, "locPlace")) {

				crit.add (
					Restrictions.eq (
						"_chatuser.locationPlace",
						value));

			} else if (equal (key, "orient")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.orient",
						value));

			} else if (equal (key, "typeIn")) {

				if (((Collection<?>) value).size () == 0)
					return ImmutableList.<Integer>of ();

				crit.add (
					Restrictions.in (
						"_chatUser.type",
						(Collection<?>) value));

			} else if (key.equals ("hasGender")) {

				crit.add (
					(Boolean) value
						? Restrictions.isNotNull (
							"_chatUser.gender")
						: Restrictions.isNull (
							"_chatUser.gender"));

			} else if (equal (key, "genderIn")) {

				if (((Collection<?>) value).size () == 0)
					return ImmutableList.<Integer>of ();

				crit.add (
					Restrictions.in (
						"_chatUser.gender",
						(Collection<?>) value));

			} else if (key.equals ("hasOrient")) {

				crit.add ((Boolean) value
					? Restrictions.isNotNull (
						"_chatUser.orient")
					: Restrictions.isNull (
						"_chatUser.orient"));

			} else if (equal (key, "orientIn")) {

				if (((Collection<?>) value).size () == 0)
					return Collections.emptyList ();

				crit.add (
					Restrictions.in (
						"_chatUser.orient",
						(Collection<?>) value));

			} else if (key.equals ("hasDateMode")) {

				crit.add ((Boolean) value
					? Restrictions.isNotNull (
						"_chatUser.dateMode")
					: Restrictions.isNull (
						"_chatUser.dateMode"));

			} else if (key.equals ("blockAll")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.blockAll",
						value));

			} else if (key.equals ("barred")) {

				crit.add (
					Restrictions.eq (
						"_chatUser.barred",
						value));

			} else if (equal (key, "creditFailedGte")) {

				crit.add (
					Restrictions.ge (
						"_chatUser.creditFailed",
						value));

			} else if (equal (key, "creditFailedLte")) {

				crit.add (
					Restrictions.le (
						"_chatUser.creditFailed",
						value));

			} else if (equal (key, "creditNoReportGte")) {

				crit.add (
					Restrictions.ge (
						"_chatUser.creditSent",
						value));

			} else if (equal (key, "creditNoReportLte")) {

				crit.add (
					Restrictions.le (
						"_chatUser.creditSent",
						value));

			} else if (equal (key, "valueSinceEverGte")) {

				crit.add (
					Restrictions.ge (
						"_chatUser.valueSinceEver",
						value));

			} else if (equal (key, "valueSinceEverLte")) {

				crit.add (
					Restrictions.le (
						"_chatUser.valueSinceEver",
						value));

			} else if (key.equals ("onlineAfter")) {

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
									value),
								Restrictions.isNull (
									"_chatUserSession.endTime")))

						.setProjection (
							Property.forName (
								"_chatUserSession.id"));

				crit.add (
					Subqueries.exists (
						onlineAfterCriteria));

			} else if (equal (key, "deliveryMethodIn")) {

				crit.add (
					Restrictions.in (
						"_chatUser.deliveryMethod",
						(Collection<?>) value));

			} else if (equal (key, "lastMessagePollBefore")) {

				crit.add (
					Restrictions.lt (
						"_chatUser.lastMessagePoll",
						value));

			} else if (equal (key, "lastActionAfter")) {

				crit.add (
					Restrictions.ge (
						"_chatUser.lastAction",
						value));

			} else if (equal (key, "lastActionBefore")) {

				crit.add (
					Restrictions.lt (
						"_chatUser.lastAction",
						value));

			} else if (key.equals ("orderBy")) {

				if (value.equals ("code")) {

					crit.addOrder (
						Order.asc (
							"_chatUser.code"));

				} else if (value.equals ("creditFailedDesc")) {

					crit.addOrder (
						Order.desc (
							"_chatUser.creditFailed"));

				} else {

					throw new IllegalArgumentException (
						stringFormat (
							"Unknown order by: %s",
							 value));

				}

			} else if (equal (key, "limit")) {

				crit.setMaxResults (
					(Integer) value);

			} else {

				throw new IllegalArgumentException (
					stringFormat (
						"Unknown search parameter: %s",
						key));

			}

		}

		return findMany (
			Integer.class,
			crit.list ());

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
	List<ChatUserRec> findWantingAd () {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec chatUser " +
				"WHERE chatUser.nextAd < :now ")

			.setTimestamp (
				"now",
				new Date ())

			.list ());

	}

	@Override
	public
	List<ChatUserRec> findWantingQuietOutbound () {

		return findMany (
			ChatUserRec.class,

			createQuery (
				"FROM ChatUserRec cu " +
				"WHERE cu.nextQuietOutbound <= :now")

			.setTimestamp (
				"now",
				new Date ())

			.list ());

	}

}
