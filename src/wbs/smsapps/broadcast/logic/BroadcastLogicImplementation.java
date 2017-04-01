package wbs.smsapps.broadcast.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	public final static
	int batchSize = 1024;

	// singleton dependencies

	@SingletonDependency
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	NumberLookupManager numberLookupManager;

	// implementation

	@Override
	public
	AddResult addNumbers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull BroadcastRec broadcast,
			@NonNull List <String> numberStrings,
			UserRec user) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"addNumbers");

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
					batchSize)
		) {

			List <NumberRec> batchNumbers =
				numberHelper.findOrCreateMany (
					taskLogger,
					numberStringsBatch);

			Pair <List <NumberRec>, List <NumberRec>> batchSplitNumbers =
				ifThenElse (
					isNotNull (
						broadcastConfig.getBlockNumberLookup ()),
					() -> numberLookupManager.splitNumbersPresent (
						broadcastConfig.getBlockNumberLookup (),
						batchNumbers),
					() -> Pair.of (
						new ArrayList <NumberRec> (),
						batchNumbers));

			// process rejected numbers

			List <NumberRec> batchRejectedNumbers =
				batchSplitNumbers.getLeft ();

			List <BroadcastNumberRec> batchRejectedBroadcastNumbers =
				broadcastNumberHelper.findOrCreateMany (
					taskLogger,
					broadcast,
					batchRejectedNumbers);

			for (
				long index = 0;
				index < collectionSize (batchRejectedNumbers);
				index ++
			) {

				BroadcastNumberRec rejectedBroadcastNumber =
					listItemAtIndexRequired (
						batchRejectedBroadcastNumbers,
						index);

				// add number

				switch (rejectedBroadcastNumber.getState ()) {

				case removed:

					taskLogger.debugFormat (
						"Reject number %s",
						rejectedBroadcastNumber.getNumber ().getNumber ());

					rejectedBroadcastNumber

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

					break;

				case accepted:

					taskLogger.debugFormat (
						"Don't reject existing number %s",
						rejectedBroadcastNumber.getNumber ().getNumber ());

					result.numAlreadyAdded ++;

					break;

				case rejected:

					taskLogger.debugFormat (
						"Already rejected number %s",
						rejectedBroadcastNumber.getNumber ().getNumber ());

					result.numAlreadyRejected ++;

					break;

				case sent:

					throw new RuntimeException (
						"Should never happen");

				}

			}

			// process accepted numbers

			List <NumberRec> batchAcceptedNumbers =
				batchSplitNumbers.getRight ();

			List <BroadcastNumberRec> batchAcceptedBroadcastNumbers =
				broadcastNumberHelper.findOrCreateMany (
					taskLogger,
					broadcast,
					batchAcceptedNumbers);

			for (
				long index = 0;
				index < collectionSize (batchAcceptedNumbers);
				index ++
			) {

				BroadcastNumberRec acceptedBroadcastNumber =
					listItemAtIndexRequired (
						batchAcceptedBroadcastNumbers,
						index);

				// add number

				switch (acceptedBroadcastNumber.getState ()) {

				case removed:

					taskLogger.debugFormat (
						"Add number %s",
						acceptedBroadcastNumber.getNumber ().getNumber ());

					acceptedBroadcastNumber

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

					break;

				case accepted:

					taskLogger.debugFormat (
						"Already added number %s",
						acceptedBroadcastNumber.getNumber ().getNumber ());

					result.numAlreadyAdded ++;

					break;

				case rejected:

					taskLogger.debugFormat (
						"Adding previously rejected number %s",
						acceptedBroadcastNumber.getNumber ().getNumber ());

					acceptedBroadcastNumber

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

					break;

				case sent:

					throw new RuntimeException (
						"Should never happen");

				}

			}

			// flush transaction

			transaction.flush ();

		}

		return result;

	}

}
