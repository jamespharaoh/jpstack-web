package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.IterableUtils.iterableZipRequired;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalEqualAndPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.string.StringUtils.joinWithComma;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.utils.function.QuadPredicate;

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
import shn.shopify.apiclient.product.ShopifyProductImageResponse;
import shn.shopify.apiclient.product.ShopifyProductListResponse;
import shn.shopify.apiclient.product.ShopifyProductOptionRequest;
import shn.shopify.apiclient.product.ShopifyProductRequest;
import shn.shopify.apiclient.product.ShopifyProductResponse;
import shn.shopify.apiclient.product.ShopifyProductVariantRequest;
import shn.shopify.apiclient.product.ShopifyProductVariantResponse;
import shn.shopify.model.ShnShopifyConnectionRec;

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
	ShopifyProductApiClient productApiClient;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

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
				productApiClient.listAllProducts (
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
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			productApiClient.removeProduct (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyProductResponse createItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return productApiClient.createProduct (
				transaction,
				credentials,
				productRequest (
					transaction,
					connection,
					localProduct));

		}

	}

	@Override
	public
	ShopifyProductResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localItem,
			@NonNull ShopifyProductResponse remoteItem) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return productApiClient.updateProduct (
				transaction,
				credentials,
				productRequest (
					transaction,
					connection,
					localItem));

		}

	}

	@Override
	public
	boolean compareItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyProductResponse remoteProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareItem");

		) {

			return QuadPredicate.allTrue (
				productComparisons,
				transaction,
				connection,
				localProduct,
				remoteProduct);

		}

	}

	// private implementation

	private
	ShopifyProductRequest productRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"productRequest");

		) {

			ShnDatabaseRec shnDatabase =
				localProduct.getDatabase ();

			ShopifyProductRequest shopifyProductRequest =
				new ShopifyProductRequest ()

				.id (
					localProduct.getShopifyId ())

				.title (
					localProduct.getPublicTitle ())

				.bodyHtml (
					shopifyLogic.productDescription (
						transaction,
						connection,
						localProduct))

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

					.id (
						localImage.getShopifyId ())

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

					.id (
						localVariant.getShopifyId ())

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

			// return

			return shopifyProductRequest;

		}

	}

	@Override
	public
	void saveShopifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyProductResponse remoteProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"saveShopifyData");

		) {

			localProduct

				.setShopifyNeedsSync (
					false)

				.setShopifyId (
					remoteProduct.id ())

				.setShopifyUpdatedAt (
					Instant.parse (
						remoteProduct.updatedAt ()))

			;

			for (
				long imageIndex = 0;
				imageIndex < collectionSize (
					localProduct.getImages ());
				imageIndex ++
			) {

				ShnProductImageRec localImage =
					listItemAtIndexRequired (
						localProduct.getImages (),
						imageIndex);

				ShopifyProductImageResponse remoteImage =
					listItemAtIndexRequired (
						remoteProduct.images (),
						imageIndex);

				localImage

					.setShopifyId (
						remoteImage.id ())

				;

			}

			List <ShnProductVariantRec> localVariantsOrdered =
				ImmutableList.copyOf (
					localProduct.getVariants ());

			for (
				long variantIndex = 0;
				variantIndex < collectionSize (
					localVariantsOrdered);
				variantIndex ++
			) {

				ShnProductVariantRec localVariant =
					listItemAtIndexRequired (
						localVariantsOrdered,
						variantIndex);

				ShopifyProductVariantResponse remoteVariant =
					listItemAtIndexRequired (
						remoteProduct.variants (),
						variantIndex);

				localVariant

					.setShopifyId (
						remoteVariant.id ())

				;

			}

		}

	}

	// data

	List <QuadPredicate <
		Transaction,
		ShnShopifyConnectionRec,
		ShnProductVariantRec,
		ShopifyProductVariantResponse
	>>
		variantComparisons =
			ImmutableList.<QuadPredicate <
				Transaction,
				ShnShopifyConnectionRec,
				ShnProductVariantRec,
				ShopifyProductVariantResponse
			>> of (

		(transaction, connection, localVariant, remoteVariant) ->
			integerEqualSafe (
				localVariant.getShopifyId (),
				remoteVariant.id ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				localVariant.getPublicTitle (),
				remoteVariant.title ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				localVariant.getItemNumber (),
				remoteVariant.sku ()),

		// TODO barcode

		// TODO image_id

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				currencyLogic.formatSimple (
					localVariant.getDatabase ().getCurrency (),
					ifNull (
						localVariant.getPromotionalPrice (),
						localVariant.getShoppingNationPrice ())),
				remoteVariant.price ()),

		(transaction, connection, localVariant, remoteVariant) ->
			ifNotNullThenElse (
				localVariant.getPromotionalPrice (),
				() -> stringEqualSafe (
					currencyLogic.formatSimple (
						localVariant.getDatabase ().getCurrency (),
						localVariant.getShoppingNationPrice ()),
					remoteVariant.compareAtPrice ()),
				() -> isNull (
					remoteVariant.compareAtPrice ())),

		(transaction, connection, localVariant, remoteVariant) ->
			booleanEqual (
				true,
				remoteVariant.taxable ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				"shopify",
				remoteVariant.inventoryManagement ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				"deny",
				remoteVariant.inventoryPolicy ()),


		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				"shopify",
				remoteVariant.inventoryManagement ()),

		// TODO inventory quantity (or not?)
		// TODO grams, weight, weightUnit (or not?)

		(translations, connecion, localVariant, remoteVariant) ->
			stringEqualSafe (
				"manual",
				remoteVariant.fulfillmentService ()),

		(transaction, connection, localVariant, remoteVariant) ->
			booleanEqual (
				true,
				remoteVariant.requiresShipping ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				"shopify",
				remoteVariant.inventoryManagement ()),

		(transaction, connection, localVariant, remoteVariant) ->
			stringEqualSafe (
				joinWithComma (
					iterableMapToList (
						productLogic.sortVariantValues (
							localVariant.getVariantValues ()),
						ShnProductVariantValueRec::getPublicTitle)),
				joinWithComma (
					presentInstances (
						optionalFromNullable (
							remoteVariant.option1 ()),
						optionalFromNullable (
							remoteVariant.option2 ()),
						optionalFromNullable (
							remoteVariant.option3 ())))),

		(transaction, connection, localVariant, remoteVariant) ->
			optionalEqualAndPresentWithClass (
				Instant.class,
				optionalFromNullable (
					localVariant.getShopifyCreatedAt ()),
				optionalOf (
					Instant.parse (
						remoteVariant.createdAt ()))),

		(transaction, connection, localVariant, remoteVariant) ->
			optionalEqualAndPresentWithClass (
				Instant.class,
				optionalFromNullable (
					localVariant.getShopifyUpdatedAt ()),
				optionalOf (
					Instant.parse (
						remoteVariant.updatedAt ())))

	);

	List <QuadPredicate <
		Transaction,
		ShnShopifyConnectionRec,
		ShnProductImageRec,
		ShopifyProductImageResponse
	>>
		imageComparisons =
			ImmutableList.of (

		(transaction, connection, localImage, remoteImage) ->
			integerEqualSafe (
				localImage.getShopifyId (),
				remoteImage.id ()),

		(transaction, connection, localImage, remoteImage) ->
			integerEqualSafe (
				localImage.getIndex (),
				remoteImage.position () - 1),

		(transaction, connection, localImage, remoteImage) ->
			referenceEqualWithClass (
				Instant.class,
				localImage.getShopifyCreatedAt (),
				Instant.parse (
					remoteImage.createdAt ())),

		(transaction, connection, localImage, remoteImage) ->
			referenceEqualWithClass (
				Instant.class,
				localImage.getShopifyUpdatedAt (),
				Instant.parse (
					remoteImage.updatedAt ())),

		(transaction, connection, localImage, remoteImage) ->
			stringEqualSafe (
				localImage.getShopifySrc (),
				remoteImage.src ())

	);

	List <QuadPredicate <
		Transaction,
		ShnShopifyConnectionRec,
		ShnProductRec,
		ShopifyProductResponse
	>>
		productComparisons =
			ImmutableList.of (

		(transaction, connection, localProduct, remoteProduct) ->
			integerEqualSafe (
				localProduct.getShopifyId (),
				remoteProduct.id ()),

		(transaction, connection, localProduct, remoteProduct) ->
			stringEqualSafe (
				localProduct.getPublicTitle (),
				remoteProduct.title ()),

		(transaction, connection, localProduct, remoteProduct) ->
			stringEqualSafe (
				shopifyLogic.productDescription (
					transaction,
					connection,
						localProduct),
				remoteProduct.bodyHtml ()),

		(transaction, connection, localProduct, remoteProduct) ->
			stringEqualSafe (
				localProduct.getSupplier ().getPublicName (),
				remoteProduct.vendor ()),

		(transaction, connection, localProduct, remoteProduct) ->
			stringEqualSafe (
				localProduct.getSubCategory ().getPublicTitle (),
				remoteProduct.productType ()),

		(transaction, connection, localProduct, remoteProduct) ->
			integerEqualSafe (
				collectionSize (
					localProduct.getVariantsNotDeleted ()),
				collectionSize (
					remoteProduct.variants ())),

		(transaction, connection, localProduct, remoteProduct) ->
			integerEqualSafe (
				collectionSize (
					localProduct.getImagesNotDeleted ()),
				collectionSize (
					remoteProduct.images ())),

		(transaction, connection, localProduct, remoteProduct) ->
			allOf (
				iterableMap (
					iterableZipRequired (
						localProduct.getVariantsNotDeleted (),
						remoteProduct.variants ()),
					(localVariant, remoteVariant) ->
						() -> QuadPredicate.allTrue (
							variantComparisons,
							transaction,
							connection,
							localVariant,
							remoteVariant))),

		(transaction, connection, localProduct, remoteProduct) ->
			allOf (
				iterableMap (
					iterableZipRequired (
						localProduct.getImagesNotDeleted (),
						remoteProduct.images ()),
					(localImage, remoteImage) ->
						() -> QuadPredicate.allTrue (
							imageComparisons,
							transaction,
							connection,
							localImage,
							remoteImage)))

	);

}
