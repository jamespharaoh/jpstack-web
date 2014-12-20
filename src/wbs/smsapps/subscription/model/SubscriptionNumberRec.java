package wbs.smsapps.subscription.model;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionNumberRec
	implements CommonRecord<SubscriptionNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionRec subscription;

	@IdentityReferenceField
	NumberRec number;

	// state

	@SimpleField
	Boolean active = false;

	@ReferenceField (
		nullable = true)
	SubscriptionSubRec activeSubscriptionSub;

	// details

	@SimpleField (
		nullable = true)
	Instant firstJoin;

	@SimpleField (
		nullable = true)
	Instant lastJoin;

	@ReferenceField (
		nullable = true)
	SubscriptionListRec subscriptionList;

	@ReferenceField (
		nullable = true)
	SubscriptionAffiliateRec subscriptionAffiliate;

	@SimpleField
	Integer numSubs = 0;

	// children

	@CollectionField (
		index = "index")
	List<SubscriptionSubRec> subscriptionSubs =
		new ArrayList<SubscriptionSubRec> ();

	// compare to

	public
	int compareTo (
			Record<SubscriptionNumberRec> otherRecord) {

		SubscriptionNumberRec other =
			(SubscriptionNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscription (),
				other.getSubscription ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

	// dao methods

	public static
	interface SubscriptionNumberDaoMethods {

		SubscriptionNumberRec find (
				SubscriptionRec subscription,
				NumberRec number);

		List<Integer> searchIds (
				SubscriptionNumberSearch search);

	}

	// object helper methods

	public static
	interface SubscriptionNumberObjectHelperMethods {

		SubscriptionNumberRec findOrCreate (
				SubscriptionRec subscription,
				NumberRec number);

	}

	// object helper implementation

	public static
	class SubscriptionNumberObjectHelperImplementation
		implements SubscriptionNumberObjectHelperMethods {

		// indirect dependencies

		@Inject
		Provider<SubscriptionNumberObjectHelper>
		subscriptionNumberHelperProvider;

		// implementation

		public
		SubscriptionNumberRec findOrCreate (
				SubscriptionRec subscription,
				NumberRec number) {

			SubscriptionNumberObjectHelper subscriptionNumberHelper =
				subscriptionNumberHelperProvider.get ();

			// find existing

			SubscriptionNumberRec subscriptionNumber =
				subscriptionNumberHelper.find (
					subscription,
					number);

			if (subscriptionNumber != null)
				return subscriptionNumber;

			// create new

			return subscriptionNumberHelper.insert (
				new SubscriptionNumberRec ()

				.setSubscription (
					subscription)

				.setNumber (
					number)

			);

		}

	}

	// object hooks

	public static
	class SubscriptionNumberHooks
		extends AbstractObjectHooks<SubscriptionNumberRec> {

		@Inject
		SubscriptionNumberDao subscriptionNumberDao;

		@Override
		public
		List<Integer> searchIds (
				Object searchObject) {

			SubscriptionNumberSearch search =
				(SubscriptionNumberSearch) searchObject;

			return subscriptionNumberDao.searchIds (
				search);

		}

	}

}
