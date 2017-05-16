package wbs.smsapps.broadcast.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast,
			@NonNull List <String> numberStrings,
			UserRec user) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addNumbers");

		) {

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
						transaction,
						numberStringsBatch);

				Pair <List <NumberRec>, List <NumberRec>> batchSplitNumbers =
					ifThenElse (
						isNotNull (
							broadcastConfig.getBlockNumberLookup ()),
						() -> numberLookupManager.splitNumbersPresent (
							transaction,
							broadcastConfig.getBlockNumberLookup (),
							batchNumbers),
						() -> Pair.of (
							emptyList (),
							batchNumbers));

				// process rejected numbers

				List <NumberRec> batchRejectedNumbers =
					batchSplitNumbers.getLeft ();

				List <BroadcastNumberRec> batchRejectedBroadcastNumbers =
					broadcastNumberHelper.findOrCreateMany (
						transaction,
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

						transaction.debugFormat (
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

						transaction.debugFormat (
							"Don't reject existing number %s",
							rejectedBroadcastNumber.getNumber ().getNumber ());

						result.numAlreadyAdded ++;

						break;

					case rejected:

						transaction.debugFormat (
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
						transaction,
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

						transaction.debugFormat (
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

						transaction.debugFormat (
							"Already added number %s",
							acceptedBroadcastNumber.getNumber ().getNumber ());

						result.numAlreadyAdded ++;

						break;

					case rejected:

						transaction.debugFormat (
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

}
