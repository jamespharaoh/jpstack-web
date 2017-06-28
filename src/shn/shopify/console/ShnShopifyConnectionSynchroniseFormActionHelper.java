package shn.shopify.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.utils.string.FormatWriter;

import shn.core.model.ShnDatabaseRec;
import shn.product.console.ShnProductConsoleHelper;
import shn.product.logic.ShnProductLogic;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueRec;
import shn.shopify.apiclient.ShopifyApiClient;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyProductListResponse;
import shn.shopify.apiclient.ShopifyProductOptionRequest;
import shn.shopify.apiclient.ShopifyProductRequest;
import shn.shopify.apiclient.ShopifyProductResponse;
import shn.shopify.apiclient.ShopifyProductVariantRequest;
import shn.shopify.model.ShnShopifyConnectionRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("shnShopifyConnectionSynchroniseFormActionHelper")
public
class ShnShopifyConnectionSynchroniseFormActionHelper
	implements ConsoleFormActionHelper <
		ShnShopifyConnectionSynchroniseForm,
		Object
	> {

	// singleton depdendencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ShnProductConsoleHelper productHelper;

	@SingletonDependency
	ShnProductLogic productLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ShopifyApiClient shopifyApiClient;

	@SingletonDependency
	ShnShopifyConnectionConsoleHelper shopifyConnectionHelper;

	// state

	ShnShopifyConnectionRec shopifyConnection;
	ShopifyApiClientCredentials shopifyCredentials;

	ShnDatabaseRec shnDatabase;

	List <ShnProductRec> localProducts;
	Map <Long, ShnProductRec> localProductsByShopifyId;
	List <ShnProductRec> localProductsWithoutShopifyId;

	List <ShopifyProductResponse> shopifyProducts;
	Map <Long, ShopifyProductResponse> shopifyProductsById;

	long productsCreated = 0l;
	long productsUpdated = 0l;
	long productsRemoved = 0l;

	long operations = 0;
	long maxOperations = 0;

	// public implementation

	@Override
	public
	Permissions canBePerformed (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canBePerformed");

		) {

			ShnShopifyConnectionRec shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			boolean allowed =
				privChecker.canRecursive (
					transaction,
					shopifyConnection,
					"admin");

			return new Permissions ()

				.canView (
					allowed)

				.canPerform (
					allowed)

			;

		}

	}

	@Override
	public
	void writePreamble (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writePreamble");

		) {

			htmlParagraphWriteFormat (
				formatWriter,
				"Use this page to synchronise the platform database with ",
				"Shopify. Select the type of updates you wish to perform.");

		}

	}

	@Override
	public
	ShnShopifyConnectionSynchroniseForm constructFormState (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"constructFormState");

		) {

			return new ShnShopifyConnectionSynchroniseForm ()

				.createProducts (
					true)

				.updateProducts (
					true)

				.removeProducts (
					true)

				.maxOperations (
					10l)

			;

		}

	}

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionSynchroniseForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			shopifyCredentials =
				shopifyApiClient.getCredentials (
					transaction,
					shopifyConnection.getStore ());

			shnDatabase =
				shopifyConnection.getDatabase ();

			maxOperations =
				formState.maxOperations ();

			findLocalProducts (
				transaction);

			findShopifyProducts (
				transaction);

			if (formState.removeProducts ()) {

				removeProducts (
					transaction);

			}

			if (formState.createProducts ()) {

				createProducts (
					transaction);

			}

			// update products

			// TODO

			// commit and return

			transaction.commit ();

			if (
				moreThanZero (
					productsCreated)
			) {

				requestContext.addNoticeFormat (
					"%s products created",
					integerToDecimalString (
						productsCreated));

			}

			if (
				moreThanZero (
					productsUpdated)
			) {

				requestContext.addNoticeFormat (
					"%s products updated",
					integerToDecimalString (
						productsUpdated));

			}

			if (
				moreThanZero (
					productsRemoved)
			) {

				requestContext.addNoticeFormat (
					"%s products removed",
					integerToDecimalString (
						productsRemoved));

			}

			if (
				moreThan (
					operations,
					formState.maxOperations ())
			) {

				requestContext.addWarningFormat (
					"Only performed %s out of %s operations, please repeat",
					integerToDecimalString (
						formState.maxOperations ()),
					integerToDecimalString (
						operations));

			}

			return optionalAbsent ();

		}

	}

	// private implementation

	private
	void findLocalProducts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalProducts");

		) {

			transaction.noticeFormat (
				"Fetching all products from database");

			localProducts =
				productHelper.findAllNotDeletedEntities (
					transaction);

			localProductsByShopifyId =
				mapWithDerivedKey (
					iterableFilter (
						localProducts,
						localProduct ->
							isNotNull (
								localProduct.getShopifyId ())),
					ShnProductRec::getShopifyId);

			localProductsWithoutShopifyId =
				iterableFilterToList (
					localProducts,
					localProduct ->
						isNull (
							localProduct.getShopifyId ()));

			transaction.noticeFormat (
				"Found %s products total, ",
				integerToDecimalString (
					collectionSize (
						localProducts)),
				"%s previously synchronised with shopify, ",
				integerToDecimalString (
					collectionSize (
						localProductsByShopifyId)),
				"%s never synchronised with shopify",
				integerToDecimalString (
					collectionSize (
						localProductsWithoutShopifyId)));

		}

	}

	private
	void findShopifyProducts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findShopifyProducts");

		) {

			transaction.noticeFormat (
				"Retrieving list of products from shopify");

			ShopifyProductListResponse shopifyProductListResponse =
				shopifyApiClient.listAllProducts (
					transaction,
					shopifyCredentials);

			shopifyProducts =
				shopifyProductListResponse.products ();

			shopifyProductsById =
				mapWithDerivedKey (
					shopifyProducts,
					ShopifyProductResponse::id);

			transaction.noticeFormat (
				"Retrieved %s products from shopify",
				integerToDecimalString (
					collectionSize (
						shopifyProducts)));

		}

	}

	private
	void removeProducts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeProducts");

		) {

			transaction.noticeFormat (
				"About to remove products from shopify");

			for (
				ShopifyProductResponse shopifyProduct
					: shopifyProducts
			) {

				if (
					mapContainsKey (
						localProductsByShopifyId,
						shopifyProduct.id ())
				) {
					continue;
				}

				operations ++;

				if (operations > maxOperations) {
					continue;
				}

				transaction.noticeFormat (
					"Removing product %s from shopify",
					integerToDecimalString (
						shopifyProduct.id ()));

				shopifyApiClient.removeProduct (
					transaction,
					shopifyCredentials,
					shopifyProduct.id ());

				productsRemoved ++;

			}

			transaction.noticeFormat (
				"Removed %s products from shopify",
				integerToDecimalString (
					productsRemoved));

		}

	}

	private
	void createProducts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createProducts");

		) {

			transaction.noticeFormat (
				"About to create products in shopify");

			for (
				ShnProductRec localProduct
					: localProductsWithoutShopifyId
			) {

				operations ++;

				if (operations > maxOperations) {
					continue;
				}

				transaction.noticeFormat (
					"Creating product %s (%s) in shopify",
					integerToDecimalString (
						localProduct.getId ()),
					localProduct.getItemNumber ());

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
							localVariantValue.getPublicTitle ());

					}

					shopifyVariantsBuilder.add (
						shopifyVariantRequest);

				}

				shopifyProductRequest.variants (
					shopifyVariantsBuilder.build ());

System.out.println (
	"VARIANTS: " + shopifyProductRequest.variants ().size ());

				ShopifyProductResponse shopifyProductResponse =
					shopifyApiClient.createProduct (
						transaction,
						shopifyCredentials,
						shopifyProductRequest);

				localProduct

					.setShopifyId (
						shopifyProductResponse.id ())

					.setShopifyUpdatedAt (
						Instant.parse (
							shopifyProductResponse.updatedAt ()))

				;

				productsCreated ++;

				transaction.noticeFormat (
					"Created product %s (%s) in shopify ",
					integerToDecimalString (
						localProduct.getId ()),
					localProduct.getItemNumber (),
					"with shopify id %s",
					integerToDecimalString (
						shopifyProductResponse.id ()));

			}

			transaction.noticeFormat (
				"Created %s products in shopify",
				integerToDecimalString (
					productsCreated));

		}

	}

}
