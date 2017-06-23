package shn.shopify.console;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

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

import wbs.utils.string.FormatWriter;

import shn.product.console.ShnProductConsoleHelper;
import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClient;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyProductListResponse;
import shn.shopify.apiclient.ShopifyProductRequest;
import shn.shopify.apiclient.ShopifyProductResponse;
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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ShnProductConsoleHelper productHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ShopifyApiClient shopifyApiClient;

	@SingletonDependency
	ShnShopifyConnectionConsoleHelper shopifyConnectionHelper;

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
					50l)

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

			ShnShopifyConnectionRec shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			long productsCreated = 0l;
			long productsUpdated = 0l;
			long productsRemoved = 0l;

			long operations = 0;

			ShopifyApiClientCredentials credentials =
				shopifyApiClient.getCredentials (
					transaction,
					shopifyConnection.getStore ());

			// find and index local products

			transaction.noticeFormat (
				"Fetching all products from database");

			List <ShnProductRec> localProducts =
				productHelper.findAllNotDeletedEntities (
					transaction);

			Map <Long, ShnProductRec> localProductsByShopifyId =
				mapWithDerivedKey (
					iterableFilter (
						localProducts,
						localProduct ->
							isNotNull (
								localProduct.getShopifyId ())),
					ShnProductRec::getShopifyId);

			List <ShnProductRec> localProductsWithoutShopifyId =
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

			// find and index shopify products

			transaction.noticeFormat (
				"Retrieving list of products from shopify");

			ShopifyProductListResponse shopifyProductListResponse =
				shopifyApiClient.listAllProducts (
					transaction,
					credentials);

			List <ShopifyProductResponse> shopifyProducts =
				shopifyProductListResponse.products ();

			Map <Long, ShopifyProductResponse> shopifyProductsById =
				mapWithDerivedKey (
					shopifyProducts,
					ShopifyProductResponse::id);

			transaction.noticeFormat (
				"Retrieved %s products from shopify",
				integerToDecimalString (
					collectionSize (
						shopifyProducts)));

			// remove products

			if (formState.removeProducts ()) {

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

					if (operations > formState.maxOperations ()) {
						continue;
					}

					transaction.noticeFormat (
						"Removing product %s from shopify",
						integerToDecimalString (
							shopifyProduct.id ()));

					shopifyApiClient.removeProduct (
						transaction,
						credentials,
						shopifyProduct.id ());

					productsRemoved ++;

				}

				transaction.noticeFormat (
					"Removed %s products from shopify",
					integerToDecimalString (
						productsRemoved));

			}

			// create products

			if (formState.createProducts ()) {

				transaction.noticeFormat (
					"About to create products in shopify");

				for (
					ShnProductRec localProduct
						: localProductsWithoutShopifyId
				) {

					operations ++;

					if (operations > formState.maxOperations ()) {
						continue;
					}

					transaction.noticeFormat (
						"Creating product %s (%s) in shopify",
						integerToDecimalString (
							localProduct.getId ()),
						localProduct.getItemNumber ());

					ShopifyProductResponse shopifyProduct =
						shopifyApiClient.createProduct (
							transaction,
							credentials,
							new ShopifyProductRequest ()

						.title (
							localProduct.getPublicTitle ())

						.bodyHtml (
							localProduct.getPublicDescription ().getText ())

						.vendor (
							localProduct.getSupplier ().getPublicName ())

						.productType (
							localProduct.getSubCategory ().getPublicName ())

						);

						localProduct

							.setShopifyId (
								shopifyProduct.id ())

							.setShopifyUpdatedAt (
								Instant.parse (
									shopifyProduct.updatedAt ()))

						;

					productsCreated ++;

					transaction.noticeFormat (
						"Created product %s (%s) in shopify ",
						integerToDecimalString (
							localProduct.getId ()),
						localProduct.getItemNumber (),
						"with shopify id %s",
						integerToDecimalString (
							shopifyProduct.id ()));

				}

				transaction.noticeFormat (
					"Created %s products in shopify",
					integerToDecimalString (
						productsCreated));

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

}
