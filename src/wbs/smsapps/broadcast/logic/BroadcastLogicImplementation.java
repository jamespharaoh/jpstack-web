package wbs.smsapps.broadcast.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;

import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;

@SingletonComponent ("broadcastLogic")
public
class BroadcastLogicImplementation
	implements BroadcastLogic {

	// singleton dependencies

	@SingletonDependency
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	NumberLookupManager numberLookupManager;

	// implementation

	@Override
	public
	AddResult addNumbers (
			@NonNull BroadcastRec broadcast,
			@NonNull List <String> numberStrings,
			UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		AddResult result =
			new AddResult ();

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		// add numbers

		for (
			List <String> numberStringsBatch
				: Lists.partition (
					numberStrings,
					256)
		) {

			List <NumberRec> numberRecords =
				numberHelper.findOrCreateMany (
					numberStringsBatch);

			List <BroadcastNumberRec> broadcastNumbers =
				broadcastNumberHelper.findOrCreateMany (
					broadcast,
					numberRecords);

			for (
				long index = 0;
				index < collectionSize (numberStringsBatch);
				index ++
			) {

				NumberRec numberRecord =
					listItemAtIndexRequired (
						numberRecords,
						index);

				BroadcastNumberRec broadcastNumber =
					listItemAtIndexRequired (
						broadcastNumbers,
						index);

				// check block list

				boolean reject =
					broadcastConfig.getBlockNumberLookup () != null
						? numberLookupManager.lookupNumber (
							broadcastConfig.getBlockNumberLookup (),
							numberRecord)
						: false;

				// add number

				switch (broadcastNumber.getState ()) {

				case removed:

					if (reject) {

						broadcastNumber

							.setState (
								BroadcastNumberState.rejected)

							.setAddedByUser (
								user);

						broadcast

							.setNumRemoved (
								broadcast.getNumRemoved () - 1)

							.setNumRejected (
								broadcast.getNumRejected () + 1);

						result.numRejected ++;

					} else {

						broadcastNumber

							.setState (
								BroadcastNumberState.accepted)

							.setAddedByUser (
								user);

						broadcast

							.setNumRemoved (
								broadcast.getNumRemoved () - 1)

							.setNumAccepted (
								broadcast.getNumAccepted () + 1)

							.setNumTotal (
								broadcast.getNumTotal () + 1);

						result.numAdded ++;

					}

					break;

				case accepted:

					result.numAlreadyAdded ++;

					break;

				case rejected:

					if (reject) {

						result.numAlreadyRejected ++;

					} else {

						broadcastNumber

							.setState (
								BroadcastNumberState.accepted)

							.setAddedByUser (
								user);

						broadcast

							.setNumRejected (
								broadcast.getNumRejected () - 1)

							.setNumAccepted (
								broadcast.getNumAccepted () + 1)

							.setNumTotal (
								broadcast.getNumTotal () + 1);

						result.numAdded ++;

					}

					break;

				case sent:

					throw new RuntimeException (
						"Should never happen");

				}

			}

			transaction.flush ();

		}

		return result;

	}

}
