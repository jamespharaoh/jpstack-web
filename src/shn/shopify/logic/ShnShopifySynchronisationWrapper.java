package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapDoesNotContainKey;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;

import wbs.utils.random.RandomLogic;

import shn.core.model.ShnDatabaseRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiResponseItem;
import shn.shopify.logic.ShnShopifySynchronisationHelper.EventType;
import shn.shopify.model.ShnShopifyConnectionRec;
import shn.shopify.model.ShnShopifyRecord;

@Accessors (fluent = true)
public
class ShnShopifySynchronisationWrapper <
	Local extends ShnShopifyRecord <Local>,
	Remote extends ShopifyApiResponseItem
>
	implements ShnShopifySynchronisation <
		ShnShopifySynchronisationWrapper <Local, Remote>,
		Local,
		Remote
	> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	// properties

	@Getter @Setter
	ShnShopifySynchronisationHelper <Local, Remote> helper;

	@Getter @Setter
	Boolean enableCreate;

	@Getter @Setter
	Boolean enableUpdate;

	@Getter @Setter
	Boolean enableRemove;

	@Getter @Setter
	Long maxOperations = 0l;

	@Getter @Setter
	ShnShopifyConnectionRec shopifyConnection;

	@Getter @Setter
	ShopifyApiClientCredentials shopifyCredentials;

	// state

	ShnDatabaseRec shnDatabase;

	List <Local> localItems;
	Map <Long, Local> localItemsByShopifyId;
	List <Local> localItemsWithoutShopifyId;

	List <Remote> remoteItems;
	Map <Long, Remote> remoteItemsById;

	@Getter
	long numCreated = 0l;

	@Getter
	long numNotCreated = 0l;

	@Getter
	long numUpdated = 0l;

	@Getter
	long numNotUpdated = 0l;

	@Getter
	long numRemoved = 0l;

	@Getter
	long numNotRemoved = 0l;

	@Getter
	long numOperations = 0;

	@Getter
	long numErrors = 0;

	// public implementation

	@Override
	public
	String friendlyNameSingular () {
		return helper.friendlyNameSingular ();
	}

	@Override
	public
	String friendlyNamePlural () {
		return helper.friendlyNamePlural ();
	}

	@Override
	public
	ShnShopifySynchronisationWrapper <Local, Remote> synchronise (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"synchronise");

		) {

			findLocalItems (
				transaction);

			findShopifyItems (
				transaction);

			if (enableRemove) {

				removeItems (
					transaction);

			}

			if (enableCreate) {

				createItems (
					transaction);

			}

			// update items

			if (enableUpdate) {

				updateItems (
					transaction);

			}

			// return

			return this;

		}

	}

	// private implementation

	private
	void findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			transaction.noticeFormat (
				"Fetching all %s from database",
				helper.friendlyNamePlural ());

			localItems =
				helper.findLocalItems (
					transaction);

			localItemsByShopifyId =
				mapWithDerivedKey (
					iterableFilter (
						localItems,
						localItem ->
							isNotNull (
								localItem.getShopifyId ())),
					ShnShopifyRecord::getShopifyId);

			localItemsWithoutShopifyId =
				iterableFilterToList (
					localItems,
					localItem ->
						isNull (
							localItem.getShopifyId ()));

			transaction.noticeFormat (
				"Found %s %s total, ",
				integerToDecimalString (
					collectionSize (
						localItems)),
				helper.friendlyNamePlural (),
				"%s previously synchronised with shopify, ",
				integerToDecimalString (
					collectionSize (
						localItemsByShopifyId)),
				"%s never synchronised with shopify",
				integerToDecimalString (
					collectionSize (
						localItemsWithoutShopifyId)));

		}

	}

	private
	void findShopifyItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findShopifyItems");

		) {

			transaction.noticeFormat (
				"Retrieving list of %s from shopify",
				helper.friendlyNamePlural ());

			remoteItems =
				helper.findRemoteItems (
					transaction,
					shopifyCredentials);

			remoteItemsById =
				mapWithDerivedKey (
					remoteItems,
					ShopifyApiResponseItem::id);

			transaction.noticeFormat (
				"Retrieved %s %s from shopify",
				integerToDecimalString (
					collectionSize (
						remoteItems)),
				helper.friendlyNamePlural ());

		}

	}

	private
	void removeItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItems");

		) {

			transaction.noticeFormat (
				"About to remove %s from shopify",
				helper.friendlyNamePlural ());

			List <Remote> selectedRemoteItems =
				randomLogic.shuffleToList (
					remoteItems)

				.stream ()

				.filter (
					remoteItem ->
						mapDoesNotContainKey (
							localItemsByShopifyId,
							remoteItem.id ()))

				.limit (
					maxOperations)

				.collect (
					Collectors.toList ())
			;

			selectedRemoteItems.parallelStream ()

				.forEach (
					remoteItem ->
						removeItem (
							transaction.parallel (),
							remoteItem))

			;

			transaction.noticeFormat (
				"Removed %s %s from shopify",
				integerToDecimalString (
					numRemoved),
				helper.friendlyNamePlural ());

		}

	}

	private
	void removeItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Remote remoteItem) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"removeItems");

		) {

			synchronized (this) {
				numOperations ++;
			}

			transaction.noticeFormat (
				"Removing %s %s from shopify",
				helper.friendlyNameSingular (),
				integerToDecimalString (
					remoteItem.id ()));

			helper.removeItem (
				transaction,
				shopifyCredentials,
				shopifyConnection,
				remoteItem.id ());

			eventLogic.createEvent (
				transaction,
				helper.eventCode (
					EventType.remove),
				remoteItem.id (),
				shopifyConnection.getStore (),
				shopifyConnection);

			synchronized (this) {
				numRemoved ++;
			}

		}

	}

	private
	void createItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItems");

		) {

			transaction.noticeFormat (
				"About to create %s in shopify",
				helper.friendlyNamePlural ());

			for (
				Local localItem
					: randomLogic.shuffleToList (
						localItems)
			) {

				// check if create is required

				if (

					isNotNull (
						localItem.getShopifyId ())

					&& mapContainsKey (
						remoteItemsById,
						localItem.getShopifyId ())

				) {
					continue;
				}

				numOperations ++;

				if (numOperations > maxOperations) {

					numNotCreated ++;

					continue;

				}

				// create item

				transaction.noticeFormat (
					"Creating %s %s in shopify",
					helper.friendlyNameSingular (),
					integerToDecimalString (
						localItem.getId ()));

				Remote remoteItem =
					helper.createItem (
						transaction,
						shopifyCredentials,
						shopifyConnection,
						localItem);

				// save changes

				helper.saveShopifyData (
					transaction,
					localItem,
					remoteItem);

				eventLogic.createEvent (
					transaction,
					helper.eventCode (
						EventType.create),
					localItem,
					shopifyConnection.getStore (),
					shopifyConnection);

				// verify update

				List <String> mismatches =
					helper.compareItem (
						transaction,
						shopifyConnection,
						localItem,
						remoteItem);

				if (
					collectionIsEmpty (
						mismatches)
				) {

					localItem.setShopifyNeedsSync (
						false);

				} else {

					mismatches.forEach (
						mismatch ->
							transaction.errorFormat (
								"Created item mismatch: %s",
								mismatch));

					numErrors ++;

				}

				// update counter

				numCreated ++;

				transaction.noticeFormat (
					"Created %s %s in shopify ",
					helper.friendlyNameSingular (),
					integerToDecimalString (
						localItem.getId ()),
					"with shopify id %s",
					integerToDecimalString (
						remoteItem.id ()));

			}

			transaction.noticeFormat (
				"Created %s %s in shopify",
				integerToDecimalString (
					numCreated),
				helper.friendlyNamePlural ());

		}

	}

	private
	void updateItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItems");

		) {

			transaction.noticeFormat (
				"About to update %s in shopify",
				helper.friendlyNamePlural ());

			for (
				Local localItem
					: randomLogic.shuffleToList (
						localItems)
			) {

				// check if update is required

				Optional <Remote> remoteItemOptional =
					optionalMapOptional (
						optionalFromNullable (
							localItem.getShopifyId ()),
						shopifyId ->
							mapItemForKey (
								remoteItemsById,
								shopifyId));

				if (
					optionalIsNotPresent (
						remoteItemOptional)
				) {
					continue;
				}

				Remote remoteItem =
					optionalGetRequired (
						remoteItemOptional);

				if (

					! localItem.getShopifyNeedsSync ()

					&& collectionIsEmpty (
						helper.compareItem (
							transaction,
							shopifyConnection,
							localItem,
							remoteItem))

				) {
					continue;
				}

				numOperations ++;

				if (numOperations > maxOperations) {

					numNotUpdated ++;

					continue;

				}

				// perform update

				transaction.noticeFormat (
					"Updating %s %s in shopify",
					helper.friendlyNameSingular (),
					integerToDecimalString (
						localItem.getId ()));

				remoteItem =
					helper.updateItem (
						transaction,
						shopifyCredentials,
						shopifyConnection,
						localItem,
						remoteItem);

				// save changes

				helper.saveShopifyData (
					transaction,
					localItem,
					remoteItem);

				eventLogic.createEvent (
					transaction,
					helper.eventCode (
						EventType.update),
					localItem,
					shopifyConnection.getStore (),
					shopifyConnection);

				// verify update

				List <String> mismatches =
					helper.compareItem (
						transaction,
						shopifyConnection,
						localItem,
						remoteItem);

				if (
					collectionIsEmpty (
						mismatches)
				) {

					localItem.setShopifyNeedsSync (
						false);

				} else {

					mismatches.forEach (
						mismatch ->
							transaction.errorFormat (
								"Updated item mismatch: %s",
								mismatch));

					numErrors ++;

				}

				// update counter

				numUpdated ++;

				transaction.noticeFormat (
					"Updated %s %s in shopify ",
					helper.friendlyNameSingular (),
					integerToDecimalString (
						localItem.getId ()),
					"with shopify id %s",
					integerToDecimalString (
						remoteItem.id ()));

			}

			transaction.noticeFormat (
				"Updated %s %s in shopify",
				integerToDecimalString (
					numUpdated),
				helper.friendlyNamePlural ());

		}

	}

}
