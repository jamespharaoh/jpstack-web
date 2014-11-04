package wbs.smsapps.broadcast.model;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BroadcastNumberRec
	implements CommonRecord<BroadcastNumberRec> {

	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	BroadcastRec broadcast;

	@IdentityReferenceField
	NumberRec number;

	// state

	@SimpleField
	BroadcastNumberState state;

	// other information

	@ReferenceField (
		nullable = true)
	UserRec addedByUser;

	@ReferenceField (
		nullable = true)
	UserRec removedByUser;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<BroadcastNumberRec> otherRecord) {

		BroadcastNumberRec other =
			(BroadcastNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getBroadcast (),
				other.getBroadcast ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

	// object helper methods

	public static
	interface BroadcastNumberObjectHelperMethods {

		BroadcastNumberRec findOrCreate (
				BroadcastRec broadcast,
				NumberRec number);

		BroadcastNumberRec find (
				BroadcastRec broadcast,
				NumberRec number);

		List<BroadcastNumberRec> findAcceptedLimit (
				BroadcastRec broadcast,
				int limit);

	}

	// object helper implementation

	public static
	class BroadcastNumberObjectHelperImplementation
		implements BroadcastNumberObjectHelperMethods {

		@Inject
		Provider<BroadcastNumberObjectHelper> broadcastNumberHelper;

		@Override
		public
		BroadcastNumberRec findOrCreate (
				@NonNull BroadcastRec broadcast,
				@NonNull NumberRec number) {

			// find existing

			BroadcastNumberRec broadcastNumber =
				broadcastNumberHelper.get ().find (
					broadcast,
					number);

			if (broadcastNumber != null)
				return broadcastNumber;

			// create new

			broadcastNumber =
				broadcastNumberHelper.get ().insert (
					new BroadcastNumberRec ()
						.setBroadcast (broadcast)
						.setNumber (number)
						.setState (BroadcastNumberState.removed));

			// update broadcast

			broadcast
				.setNumRemoved (broadcast.getNumRemoved () + 1);

			// return

			return broadcastNumber;

		}

		@Override
		public
		BroadcastNumberRec find (
				BroadcastRec broadcast,
				NumberRec number) {

			return broadcastNumberHelper.get ()
				.findByBroadcastAndNumber (
					broadcast.getId (),
					number.getId ());

		}

		@Override
		public
		List<BroadcastNumberRec> findAcceptedLimit (
				BroadcastRec broadcast,
				int limit) {

			return broadcastNumberHelper.get ()
				.findAcceptedByBroadcastLimit (
					broadcast.getId (),
					limit);

		}

	}

	// dao

	public static
	interface BroadcastNumberDaoMethods {

		BroadcastNumberRec findByBroadcastAndNumber (
				int broadcastId,
				int numberId);

		List<BroadcastNumberRec> findAcceptedByBroadcastLimit (
				int broadcastId,
				int limit);

	}

}
