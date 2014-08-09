package wbs.smsapps.broadcast.model;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.smsapps.broadcast.logic.BroadcastLogic;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BroadcastRec
	implements CommonRecord<BroadcastRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	BroadcastConfigRec broadcastConfig;

	@IndexField (
		counter = "numTotal")
	Integer index;

	// details

	@DescriptionField
	String description;

	// state

	@SimpleField
	BroadcastState state =
		BroadcastState.unsent;

	// message details

	@SimpleField
	String messageOriginator = "";

	@SimpleField
	String messageText = "";

	// various timestamps

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant scheduledTime;

	@SimpleField (
		nullable = true)
	Instant sentTime;

	@SimpleField (
		nullable = true)
	Instant cancelledTime;

	// various involved users

	@ReferenceField
	UserRec createdUser;

	@ReferenceField (
		nullable = true)
	UserRec sentUser;

	@ReferenceField (
		nullable = true)
	UserRec cancelledUser;

	// statistics about numbers

	@SimpleField
	Integer numRemoved = 0;

	@SimpleField
	Integer numAccepted = 0;

	@SimpleField
	Integer numRejected = 0;

	@SimpleField
	Integer numSent = 0;

	public
	Integer getNumTotal () {
		return getNumAccepted () + getNumSent ();
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<BroadcastRec> otherRecord) {

		BroadcastRec other =
			(BroadcastRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.toComparison ();

	}

	// object hooks

	public static
	class BroadcastHooks
		extends AbstractObjectHooks<BroadcastRec> {

		// dependencies

		@Inject
		Database database;

		// indirect dependencies

		@Inject
		Provider<BatchObjectHelper> batchHelper;

		@Inject
		Provider<BatchLogic> batchLogic;

		@Inject
		Provider<BroadcastLogic> broadcastLogicProvider;

		@Inject
		Provider<NumberFormatLogic> numberFormatLogicProvider;

		@Inject
		Provider<ObjectTypeDao> objectTypeDao;

		// implementation

		@Override
		public
		void beforeInsert (
				BroadcastRec broadcast) {

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			// set index

			broadcast.setIndex (
				broadcastConfig.getNumTotal ());

		}

		@Override
		public
		void afterInsert (
				BroadcastRec broadcast) {

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			// update parent counts

			broadcastConfig.setNumTotal (
				broadcastConfig.getNumTotal () + 1);

			broadcastConfig.setNumUnsent (
				broadcastConfig.getNumUnsent () + 1);

			// create batch

			BatchSubjectRec batchSubject =
				batchLogic.get ().batchSubject (
					broadcastConfig,
					"broadcast");

			ObjectTypeRec broadcastObjectType =
				objectTypeDao.get ().findByCode ("broadcast");

			if (broadcastObjectType == null)
				throw new NullPointerException ();

			batchHelper.get ().insert (
				new BatchRec ()
					.setParentObjectType (broadcastObjectType)
					.setParentObjectId (broadcast.getId ())
					.setCode ("broadcast")
					.setSubject (batchSubject));

			// add default numbers

			if (broadcastConfig.getDefaultNumbers () != null) {

				List<String> numbers;

				try {

					numbers =
						numberFormatLogicProvider.get ().parseLines (
							broadcastConfig.getNumberFormat (),
							broadcastConfig.getDefaultNumbers ());

				} catch (WbsNumberFormatException exception) {

					throw new RuntimeException (
						"Number format error parsing default numbers",
						exception);

				}

				broadcastLogicProvider.get ().addNumbers (
					broadcast,
					numbers,
					null);

			}

		}

	}

	// dao

	public static
	interface BroadcastDaoMethods {

		List<BroadcastRec> findSending ();

		List<BroadcastRec> findScheduled (
				Instant now);

	}

}
