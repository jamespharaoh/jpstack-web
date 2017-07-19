package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElement;
import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElement;
import static wbs.utils.collection.CollectionUtils.listThirdElement;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
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
import shn.shopify.apiclient.product.ShopifyProductImageResponse;
import shn.shopify.apiclient.product.ShopifyProductListResponse;
import shn.shopify.apiclient.product.ShopifyProductOptionRequest;
import shn.shopify.apiclient.product.ShopifyProductRequest;
import shn.shopify.apiclient.product.ShopifyProductResponse;
import shn.shopify.apiclient.product.ShopifyProductVariantRequest;
import shn.shopify.apiclient.product.ShopifyProductVariantResponse;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifyProductSynchronisationHelper")
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

	@Override
	public
	String eventCode (
			@NonNull EventType eventType) {

		switch (eventType) {

		case create:

			return stringFormat (
				"shopping_nation_product_created_in_shopify");

		case update:

			return stringFormat (
				"shopping_nation_product_updated_in_shopify");

		case remove:

			return stringFormat (
				"shopping_nation_product_removed_in_shopify");

		default:

			throw shouldNeverHappen ();

		}

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

			ShopifySynchronisationAttribute.Context <
				ShnProductRec,
				ShopifyProductRequest,
				ShopifyProductResponse
			> context =
				new ShopifySynchronisationAttribute.Context <
					ShnProductRec,
					ShopifyProductRequest,
					ShopifyProductResponse
				> ()

				.transaction (
					transaction)

				.connection (
					connection)

				.local (
					localProduct)

				.remoteResponse (
					remoteProduct)

			;

			for (
				ShopifySynchronisationAttribute <
					ShnProductRec,
					ShopifyProductRequest,
					ShopifyProductResponse
				> attribute
					: productAttributes
			) {

				if (! attribute.compare ()) {
					continue;
				}

				Optional <Object> localValue =
					attribute.localGetOperation ().apply (
						context);

				Optional <Object> remoteValue =
					attribute.remoteGetOperation ().apply (
						context);

				boolean equal =
					attribute.compareOperation ().test (
						localValue,
						remoteValue);

				if (! equal) {
					return false;
				}

			}

			return true;

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
							currencyLogic.formatSimple (
								shnDatabase.getCurrency (),
								sum (
									localVariant.getPromotionalPrice (),
									localVariant.getPostageAndPackaging ())))

						.compareAtPrice (
							currencyLogic.formatSimple (
								shnDatabase.getCurrency (),
								sum (
									localVariant.getShoppingNationPrice (),
									localVariant.getPostageAndPackaging ())))

					;

				} else {

					shopifyVariantRequest

						.price (
							currencyLogic.formatSimple (
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

	ShopifySynchronisationAttribute.Factory <
		ShnProductVariantRec,
		ShopifyProductVariantRequest,
		ShopifyProductVariantResponse
	> variantAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductVariantRec,
		ShopifyProductVariantRequest,
		ShopifyProductVariantResponse
	>> variantAttributes =
		ImmutableList.of (

		// general

		variantAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductVariantResponse::id,
			ShnProductVariantRec::setShopifyId,
			ShnProductVariantRec::getShopifyId,
			ShopifyProductVariantRequest::id),

		variantAttributeFactory.sendSimple (
			String.class,
			"public title",
			ShnProductVariantRec::getPublicTitle,
			ShopifyProductVariantRequest::title,
			ShopifyProductVariantResponse::title),

		variantAttributeFactory.sendSimple (
			String.class,
			"item number",
			ShnProductVariantRec::getItemNumber,
			ShopifyProductVariantRequest::sku,
			ShopifyProductVariantResponse::sku),

		variantAttributeFactory.sendSimple (
			String.class,
			"price",
			localVariant ->
				currencyLogic.formatSimple (
					localVariant.getDatabase ().getCurrency (),
					ifNull (
						localVariant.getPromotionalPrice (),
						localVariant.getShoppingNationPrice ())),
			ShopifyProductVariantRequest::price,
			ShopifyProductVariantResponse::price),

		variantAttributeFactory.sendSimple (
			String.class,
			"compare at price",
			localVariant ->
				optionalOrNull (
					optionalMapRequired (
						optionalFromNullable (
							localVariant.getPromotionalPrice ()),
						promotionalPrice ->
							currencyLogic.formatText (
								localVariant.getDatabase ().getCurrency (),
								localVariant.getShoppingNationPrice ()))),
			ShopifyProductVariantRequest::compareAtPrice,
			ShopifyProductVariantResponse::compareAtPrice),

		variantAttributeFactory.sendSimple (
			Boolean.class,
			"taxable",
			localVariant -> true,
			ShopifyProductVariantRequest::taxable,
			ShopifyProductVariantResponse::taxable),

		variantAttributeFactory.sendSimple (
			String.class,
			"inventory management",
			localVariant -> "shopify",
			ShopifyProductVariantRequest::inventoryManagement,
			ShopifyProductVariantResponse::inventoryManagement),

		variantAttributeFactory.sendSimple (
			String.class,
			"inventory policy",
			localVariant -> "deny",
			ShopifyProductVariantRequest::inventoryPolicy,
			ShopifyProductVariantResponse::inventoryPolicy),

		// TODO inventory quantity (or not?)
		// TODO grams, weight, weightUnit (or not?)

		// delivery

		variantAttributeFactory.sendSimple (
			String.class,
			"fulfullment service",
			localVariant -> "manual",
			ShopifyProductVariantRequest::fulfillmentService,
			ShopifyProductVariantResponse::fulfillmentService),

		variantAttributeFactory.sendSimple (
			Boolean.class,
			"requires shipping",
			localVariant -> true,
			ShopifyProductVariantRequest::requiresShipping,
			ShopifyProductVariantResponse::requiresShipping),

		// options

		variantAttributeFactory.sendSimple (
			String.class,
			"option 1",
			localVariant ->
				optionalOrNull (
					listFirstElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle))),
			ShopifyProductVariantRequest::option1,
			ShopifyProductVariantResponse::option1),

		variantAttributeFactory.sendSimple (
			String.class,
			"option 2",
			localVariant ->
				optionalOrNull (
					listSecondElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle))),
			ShopifyProductVariantRequest::option2,
			ShopifyProductVariantResponse::option2),

		variantAttributeFactory.sendSimple (
			String.class,
			"option 3",
			localVariant ->
				optionalOrNull (
					listThirdElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle))),
			ShopifyProductVariantRequest::option3,
			ShopifyProductVariantResponse::option3),

		// miscellaneous

		variantAttributeFactory.receiveSimple (
			Instant.class,
			"created at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.createdAt ()),
			ShnProductVariantRec::setShopifyCreatedAt,
			ShnProductVariantRec::getShopifyCreatedAt),

		variantAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.updatedAt ()),
			ShnProductVariantRec::setShopifyUpdatedAt,
			ShnProductVariantRec::getShopifyUpdatedAt)

	);

	ShopifySynchronisationAttribute.Factory <
		ShnProductImageRec,
		ShopifyProductImageRequest,
		ShopifyProductImageResponse
	> imageAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductImageRec,
		ShopifyProductImageRequest,
		ShopifyProductImageResponse
	>> imageAttributes =
		ImmutableList.of (

		// general

		imageAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductImageResponse::id,
			ShnProductImageRec::setShopifyId,
			ShnProductImageRec::getShopifyId,
			ShopifyProductImageRequest::id),

		// TODO position?

		// TODO attachment

		imageAttributeFactory.receiveSimple (
			String.class,
			"src",
			ShopifyProductImageResponse::src,
			ShnProductImageRec::setShopifySrc,
			ShnProductImageRec::getShopifySrc),

		// miscellaneous

		imageAttributeFactory.receiveSimple (
			Instant.class,
			"created at",
			remoteImage ->
				Instant.parse (
					remoteImage.createdAt ()),
			ShnProductImageRec::setShopifyCreatedAt,
			ShnProductImageRec::getShopifyCreatedAt),

		imageAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteImage ->
				Instant.parse (
					remoteImage.updatedAt ()),
			ShnProductImageRec::setShopifyUpdatedAt,
			ShnProductImageRec::getShopifyUpdatedAt)

	);

	ShopifySynchronisationAttribute.Factory <
		ShnProductRec,
		ShopifyProductRequest,
		ShopifyProductResponse
	> productAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductRec,
		ShopifyProductRequest,
		ShopifyProductResponse
	>> productAttributes =
		ImmutableList.of (

		// general

		productAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductResponse::id,
			ShnProductRec::setShopifyId,
			ShnProductRec::getShopifyId,
			ShopifyProductRequest::id),

		productAttributeFactory.sendSimple (
			String.class,
			"public title",
			ShnProductRec::getPublicTitle,
			ShopifyProductRequest::title,
			ShopifyProductResponse::title),

		productAttributeFactory.send (
			String.class,
			"body html",
			context ->
				optionalOf (
					shopifyLogic.productDescription (
						context.transaction (),
						context.connection (),
						context.local ())),
			(context, value) ->
				context.remoteRequest ().bodyHtml (
					(String)
					optionalGetRequired (
						value)),
			context ->
				optionalFromNullable (
					context.remoteResponse ().bodyHtml ())),

		productAttributeFactory.sendSimple (
			String.class,
			"vendor",
			localProduct ->
				localProduct.getSupplier ().getPublicName (),
			ShopifyProductRequest::vendor,
			ShopifyProductResponse::vendor),

		productAttributeFactory.sendSimple (
			String.class,
			"product type",
			localProduct ->
				localProduct.getSubCategory ().getPublicTitle (),
			ShopifyProductRequest::productType,
			ShopifyProductResponse::productType)

		// collections

		// TODO variants
		// TODO images

	);

}
