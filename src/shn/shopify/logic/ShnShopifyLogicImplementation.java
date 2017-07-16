package shn.shopify.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.model.ShnShopifyStoreRec;

@SingletonComponent ("shnShopifyLogicImplementation")
public
class ShnShopifyLogicImplementation
	implements ShnShopifyLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	ShopifyApiClientCredentials getApiCredentials (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyStoreRec shopifyStore) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getCredentials");

		) {

			return new ShopifyApiClientCredentials ()

				.storeName (
					shopifyStore.getStoreName ())

				.username (
					shopifyStore.getApiKey ())

				.password (
					shopifyStore.getPassword ())

			;

		}

	}

}
