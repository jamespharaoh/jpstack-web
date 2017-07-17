package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.MapUtils.mapContainsKey;
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

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.random.RandomLogic;

import shn.core.model.ShnDatabaseRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiResponseItem;
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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	// properties

	@Getter @Setter
	ComponentProvider <ShnShopifySynchronisationHelper <Local, Remote>>
		helperProvider;

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

	ShnShopifySynchronisationHelper <Local, Remote> helper;

	ShnDatabaseRec shnDatabase;

	List <Local> localItems;
	Map <Long, Local> localItemsByShopifyId;
	List <Local> localItemsWithoutShopifyId;

	List <Remote> remoteItems;
	Map <Long, Remote> remoteItemsById;

	@Getter
	long numCreated = 0l;

	@Getter
	long numUpdated = 0l;

	@Getter
	long numRemoved = 0l;

	@Getter
	long numOperations = 0;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			helper =
				helperProvider.provide (
					taskLogger);

		}

	}

	// public implementation

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

			for (
				Remote remoteItem
					: randomLogic.shuffleToList (
						remoteItems)
			) {

				if (
					mapContainsKey (
						localItemsByShopifyId,
						remoteItem.id ())
				) {
					continue;
				}

				numOperations ++;

				if (numOperations > maxOperations) {
					continue;
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

				numRemoved ++;

			}

			transaction.noticeFormat (
				"Removed %s %s from shopify",
				integerToDecimalString (
					numRemoved),
				helper.friendlyNamePlural ());

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

					&& helper.compareItem (
						transaction,
						shopifyConnection,
						localItem,
						remoteItem)

				) {
					continue;
				}

				numOperations ++;

				if (numOperations > maxOperations) {
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
