package wbs.smsapps.broadcast.model;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.database.Database;
import wbs.framework.object.AbstractObjectHooks;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.smsapps.broadcast.logic.BroadcastLogic;

public
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

			.setParentObjectType (
				broadcastObjectType)

			.setParentObjectId (
				broadcast.getId ())

			.setCode (
				"broadcast")

			.setSubject (
				batchSubject)

		);

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