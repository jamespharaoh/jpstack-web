package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;

import shn.core.model.ShnDatabaseRec;
import shn.product.logic.ShnProductLogic;
import shn.product.model.ShnProductImageRec;
import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.product.ShopifyProductApiClient;
import shn.shopify.apiclient.product.ShopifyProductImageRequest;
import shn.shopify.apiclient.product.ShopifyProductListResponse;
import shn.shopify.apiclient.product.ShopifyProductOptionRequest;
import shn.shopify.apiclient.product.ShopifyProductRequest;
import shn.shopify.apiclient.product.ShopifyProductResponse;
import shn.shopify.apiclient.product.ShopifyProductVariantRequest;

@PrototypeComponent ("shnShopifyProductSynchronisationHelper")
public
class ShnShopifyProductSynchronisationHelper
	implements ShnShopifySynchronisationHelper <
		ShnProductRec,
		ShopifyProductResponse
	> {

	// singleton dependecies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductObjectHelper productHelper;

	@SingletonDependency
	ShnProductLogic productLogic;

	@SingletonDependency
	ShopifyProductApiClient shopifyApiClient;

	@SingletonDependency
	WbsConfig wbsConfig;

	// details

	@Override
	public
	String friendlyNameSingular () {
		return "product";
	}

	@Override
	public
	String friendlyNamePlural () {
		return "products";
	}

	// public implementation

	@Override
	public
	List <ShnProductRec> findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			return productHelper.findNotDeleted (
				transaction);

		}

	}

	@Override
	public
	List <ShopifyProductResponse> findRemoteItems (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRemoteItems");

		) {

			ShopifyProductListResponse shopifyProductListResponse =
				shopifyApiClient.listAllProducts (
					transaction,
					credentials);

			return shopifyProductListResponse.products ();

		}

	}

	@Override
	public
	void removeItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			shopifyApiClient.removeProduct (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyProductResponse createItem (
			@NonNull NestedTransaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			ShnDatabaseRec shnDatabase =
				localProduct.getDatabase ();

			ShopifyProductRequest shopifyProductRequest =
				new ShopifyProductRequest ()

				.title (
					localProduct.getPublicTitle ())

				.bodyHtml (
					localProduct.getPublicDescription ().getText ())

				.vendor (
					localProduct.getSupplier ().getPublicName ())

				.productType (
					localProduct.getSubCategory ().getPublicTitle ())

			;

			// add options

			List <ShnProductVariantTypeRec> localVariantTypes =
				ImmutableList.copyOf (
					iterable (
						localProduct.getVariants ().stream ()

				.flatMap (
					localProductVariant ->
						localProductVariant.getVariantValues ().stream ())

				.map (
					localVariantValue ->
						localVariantValue.getType ())

				.distinct ()

				.sorted (
					Ordering.natural ().onResultOf (
						ShnProductVariantTypeRec::getCode))

				.iterator ()

			));

			if (
				collectionIsNotEmpty (
					localVariantTypes)
			) {

				ImmutableList.Builder <ShopifyProductOptionRequest>
					shopifyOptionsBuilder =
						ImmutableList.builder ();

				for (
					ShnProductVariantTypeRec localVariantType
						: localVariantTypes
				) {

					shopifyOptionsBuilder.add (
						new ShopifyProductOptionRequest ()

						.name (
							localVariantType.getPublicTitle ())

					);

				}

				shopifyProductRequest.options (
					shopifyOptionsBuilder.build ());

			}

			// add images

			ImmutableList.Builder <ShopifyProductImageRequest>
				shopifyImagesBuilder =
					ImmutableList.builder ();

			for (
				ShnProductImageRec localImage
					: localProduct.getImages ()
			) {

				ShopifyProductImageRequest shopifyImageRequest =
					new ShopifyProductImageRequest ()

					.attachment (
						bytesToBase64 (
							localImage
								.getOriginalMedia ()
								.getContent ()
								.getData ()))

				;

				shopifyImagesBuilder.add (
					shopifyImageRequest);

			}

			shopifyProductRequest.images (
				shopifyImagesBuilder.build ());

			// add variants

			ImmutableList.Builder <ShopifyProductVariantRequest>
				shopifyVariantsBuilder =
					ImmutableList.builder ();

			for (
				ShnProductVariantRec localVariant
					: localProduct.getVariants ()
			) {

				ShopifyProductVariantRequest shopifyVariantRequest =
					new ShopifyProductVariantRequest ()

					.sku (
						localVariant.getItemNumber ())

					.title (
						localVariant.getPublicTitle ())

					.inventoryManagement (
						"shopify")

					.inventoryPolicy (
						"deny")

					.inventoryQuantity (
						localVariant.getStockQuantity ())

				;

				if (
					isNotNull (
						localVariant.getPromotionalPrice ())
				) {

					shopifyVariantRequest

						.price (
							currencyLogic.toFloat (
								shnDatabase.getCurrency (),
								sum (
									localVariant.getPromotionalPrice (),
									localVariant.getPostageAndPackaging ())))

						.compareAtPrice (
							currencyLogic.toFloat (
								shnDatabase.getCurrency (),
								sum (
									localVariant.getShoppingNationPrice (),
									localVariant.getPostageAndPackaging ())))

					;

				} else {

					shopifyVariantRequest

						.price (
							currencyLogic.toFloat (
								shnDatabase.getCurrency (),
								sum (
									localVariant.getShoppingNationPrice (),
									localVariant.getPostageAndPackaging ())))

						.compareAtPrice (
							null)

					;

				}

				for (
					ShnProductVariantValueRec localVariantValue
						: productLogic.sortVariantValues (
							localVariant.getVariantValues ())
				) {

					long optionIndex =
						listIndexOfRequired (
							localVariantTypes,
							localVariantValue.getType ());

					propertySetSimple (
						shopifyVariantRequest,
						stringFormat (
							"option%s",
							integerToDecimalString (
								optionIndex + 1)),
						String.class,
						optionalOf (
							localVariantValue.getPublicTitle ()));

				}

				shopifyVariantsBuilder.add (
					shopifyVariantRequest);

			}

			shopifyProductRequest.variants (
				shopifyVariantsBuilder.build ());

			// call shopify api

			return shopifyApiClient.createProduct (
				transaction,
				credentials,
				shopifyProductRequest);

		}

	}

	@Override
	public
	ShopifyProductResponse updateItem (
			@NonNull NestedTransaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnProductRec localItem,
			@NonNull ShopifyProductResponse remoteItem) {

		throw todo ();

	}

}

