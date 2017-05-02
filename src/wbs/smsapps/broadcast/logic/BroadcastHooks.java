package wbs.smsapps.broadcast.logic;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastRec;

public
class BroadcastHooks
	implements ObjectHooks <BroadcastRec> {

	// singleton dependencies

	@WeakSingletonDependency
	BatchObjectHelper batchHelper;

	@WeakSingletonDependency
	BatchLogic batchLogic;

	@WeakSingletonDependency
	BroadcastLogic broadcastLogicProvider;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	NumberFormatLogic numberFormatLogicProvider;

	@WeakSingletonDependency
	ObjectTypeDao objectTypeDao;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			// set index

			broadcast.setIndex (
				broadcastConfig.getNumTotal ());

		}

	}

	@Override
	public
	void afterInsert (
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"afterInsert");

		) {

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			// update parent counts

			broadcastConfig.setNumTotal (
				broadcastConfig.getNumTotal () + 1);

			broadcastConfig.setNumUnsent (
				broadcastConfig.getNumUnsent () + 1);

			// create batch

			BatchSubjectRec batchSubject =
				batchLogic.batchSubject (
					transaction,
					broadcastConfig,
					"broadcast");

			ObjectTypeRec broadcastObjectType =
				objectTypeDao.findByCode (
					transaction,
					"broadcast");

			if (broadcastObjectType == null)
				throw new NullPointerException ();

			batchHelper.insert (
				transaction,
				batchHelper.createInstance ()

				.setParentType (
					broadcastObjectType)

				.setParentId (
					broadcast.getId ())

				.setCode (
					"broadcast")

				.setSubject (
					batchSubject)

			);

			// add default numbers

			if (broadcastConfig.getDefaultNumbers () != null) {

				List <String> numbers;

				try {

					numbers =
						numberFormatLogicProvider.parseLines (
							broadcastConfig.getNumberFormat (),
							broadcastConfig.getDefaultNumbers ());

				} catch (WbsNumberFormatException exception) {

					throw new RuntimeException (
						"Number format error parsing default numbers",
						exception);

				}

				broadcastLogicProvider.addNumbers (
					transaction,
					broadcast,
					numbers,
					null);

			}

		}

	}

}