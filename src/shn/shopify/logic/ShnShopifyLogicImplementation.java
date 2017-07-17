package shn.shopify.logic;

import static wbs.utils.string.PlaceholderUtils.placeholderMapCurlyBraces;
import static wbs.web.utils.HtmlUtils.htmlEncodeNewlineToBr;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.model.ShnShopifyConnectionRec;
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

	@Override
	public
	String productDescription (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec product) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"productDescriptionFromTemplate");

		) {

			return placeholderMapCurlyBraces (
				connection.getProductDescriptionTemplate ().getText (),
				ImmutableMap.<String, String> builder ()

				.put (
					"description",
					htmlEncodeNewlineToBr (
						product.getPublicDescription ().getText ()))

				.put (
					"contents",
					htmlEncodeNewlineToBr (
						product.getPublicContents ().getText ()))

				.build ()

			);

		}

	}

}
