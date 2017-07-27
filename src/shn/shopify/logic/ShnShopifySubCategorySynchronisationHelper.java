package shn.shopify.logic;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import shn.product.model.ShnProductSubCategoryObjectHelper;
import shn.product.model.ShnProductSubCategoryRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionApiClient;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionRequest;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionResponse;
import shn.shopify.apiclient.metafield.ShopifyMetafieldApiClient;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifySubCategorySynchronisationHelper")
public
class ShnShopifySubCategorySynchronisationHelper
	implements ShnShopifySynchronisationHelper <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionRequest,
		ShopifyCustomCollectionResponse
	> {

	// singleton dependecies

	@SingletonDependency
	ShopifyCustomCollectionApiClient collectionApiClient;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShopifyMetafieldApiClient metafieldApiClient;

	@SingletonDependency
	ShnProductSubCategoryObjectHelper subCategoryHelper;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	// details

	@Override
	public
	ObjectHelper <ShnProductSubCategoryRec> objectHelper () {
		return subCategoryHelper;
	}

	@Override
	public
	String friendlyNameSingular () {
		return "sub category";
	}

	@Override
	public
	String friendlyNamePlural () {
		return "sub categories";
	}

	@Override
	public
	Long getShopifyId (
			@NonNull ShnProductSubCategoryRec localItem) {

		return localItem.getShopifyId ();

	}

	@Override
	public
	Boolean getShopifyNeedsSync (
			@NonNull ShnProductSubCategoryRec localItem) {

		return localItem.getShopifyNeedsSync ();

	}

	@Override
	public
	void setShopifyNeedsSync (
			@NonNull ShnProductSubCategoryRec localItem,
			@NonNull Boolean value) {

		localItem.setShopifyNeedsSync (
			value);

	}

	@Override
	public
	String eventCode (
			@NonNull EventType eventType) {

		switch (eventType) {

		case create:

			return stringFormat (
				"shopping_nation_sub_category_created_in_shopify");

		case update:

			return stringFormat (
				"shopping_nation_sub_category_updated_in_shopify");

		case remove:

			return stringFormat (
				"shopping_nation_sub_category_removed_in_shopify");

		default:

			throw shouldNeverHappen ();

		}

	}

	// public implementation

	@Override
	public
	Optional <ShnProductSubCategoryRec> findLocalItem (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItem");

		) {

			Optional <ShnProductSubCategoryRec> subCategoryOptional =
				subCategoryHelper.find (
					transaction,
					id);

			if (
				optionalIsNotPresent (
					subCategoryOptional)
			) {
				return optionalAbsent ();
			}

			ShnProductSubCategoryRec subCategory =
				optionalGetRequired (
					subCategoryOptional);

			if (subCategory.getDeleted ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				subCategory);

		}

	}

	@Override
	public
	List <ShnProductSubCategoryRec> findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			return subCategoryHelper.findNotDeleted (
				transaction);

		}

	}

	@Override
	public
	List <ShopifyCustomCollectionResponse> findRemoteItems (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRemoteItems");

		) {

			List <ShopifyCustomCollectionResponse> customCollections =
				collectionApiClient.listAll (
					transaction,
					credentials);

/*
			Map <Pair <Long, String>, ShopifyMetafieldResponse> metafields =
				mapWithDerivedKey (
					metafieldApiClient.listByNamespaceAndOwnerResource (
						transaction,
						credentials,
						"shn-backend",
						"custom_collection"
					),
					metafield ->
						Pair.of (
							metafield.ownerId (),
							metafield.key ()));

metafields.values ().forEach (
	metafield ->
		debugFormat (
			"METAFIELD %s",
			integerToDecimalString (
				metafield.id ())));

			customCollections.forEach (
				customColection ->
					customColection.metafields (
						mapFilterByKeyToList (
							metafields,
							(id, key) ->
								integerEqualSafe (
									id,
									customColection.id ()))));

customCollections.forEach (
	customCollection -> {

	debugFormat (
		"CUSTOM COLLECTION %s",
		customCollection.title ());

	customCollection.metafields ().forEach (
		metafield ->
			debugFormat (
				"  %s = %s",
				metafield.key (),
				metafield.value ()));

});
*/

			return customCollections;

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

			collectionApiClient.remove (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse createRemoteItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCustomCollectionRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return collectionApiClient.create (
				transaction,
				credentials,
				request);

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCustomCollectionRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return collectionApiClient.update (
				transaction,
				credentials,
				request);

		}

	}

	@Override
	public
	List <String> compareItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductSubCategoryRec localSubCategory,
			@NonNull ShopifyCustomCollectionResponse remoteCollection) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareItem");

		) {

			return ImmutableList.<String> builder ()

				.addAll (
					shopifyLogic.compareAttributes (
						transaction,
						connection,
						subCategoryAttributes,
						localSubCategory,
						remoteCollection))

				.build ()

			;

		}

	}

	@Override
	public
	ShopifyCustomCollectionRequest localToRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductSubCategoryRec localSubCategory) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localToRequest");

		) {

			return shopifyLogic.createRequest (
				transaction,
				connection,
				subCategoryAttributes,
				localSubCategory,
				ShopifyCustomCollectionRequest.class)

/*
				.metafields (
					ImmutableList.of (

					ShopifyMetafieldRequest.of (
						"shn-backend",
						"type",
						"sub-category"),

					ShopifyMetafieldRequest.of (
						"shn-backend",
						"local-id",
						localSubCategory.getId ())

				))
*/

			;

		}

	}

	@Override
	public
	void updateLocalItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductSubCategoryRec localSubCategory,
			@NonNull ShopifyCustomCollectionResponse remoteCollection) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"saveShopifyData");

		) {

			localSubCategory

				.setShopifyId (
					remoteCollection.id ())

				.setShopifyUpdatedAt (
					Instant.parse (
						remoteCollection.updatedAt ()))

			;

		}

	}

	// data

	ShopifySynchronisationAttribute.Factory <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionRequest,
		ShopifyCustomCollectionResponse
	> subCategoryAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionRequest,
		ShopifyCustomCollectionResponse
	>> subCategoryAttributes =
		ImmutableList.of (

		// general

		subCategoryAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyCustomCollectionResponse::id,
			ShnProductSubCategoryRec::setShopifyId,
			ShnProductSubCategoryRec::getShopifyId,
			ShopifyCustomCollectionRequest::id),

		subCategoryAttributeFactory.sendSimple (
			String.class,
			"title",
			ShnProductSubCategoryRec::getPublicTitle,
			ShopifyCustomCollectionRequest::title,
			ShopifyCustomCollectionResponse::title),

		subCategoryAttributeFactory.sendSimple (
			String.class,
			"body html",
			localSubCategory ->
				ifNotNullThenElse (
					localSubCategory.getPublicDescription (),
					() -> localSubCategory.getPublicDescription ().getText (),
					() -> ""),
			ShopifyCustomCollectionRequest::bodyHtml,
			ShopifyCustomCollectionResponse::bodyHtml),

		// miscellaneous

		subCategoryAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.updatedAt ()),
			ShnProductSubCategoryRec::setShopifyUpdatedAt,
			ShnProductSubCategoryRec::getShopifyUpdatedAt)

	);

}
