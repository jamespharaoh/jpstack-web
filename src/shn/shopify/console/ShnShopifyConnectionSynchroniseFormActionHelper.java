package shn.shopify.console;

import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.pluralise;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductSubCategoryRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.collect.ShopifyCollectResponse;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionResponse;
import shn.shopify.apiclient.product.ShopifyProductResponse;
import shn.shopify.logic.ShnShopifyLogic;
import shn.shopify.logic.ShnShopifySynchronisation;
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
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	@SingletonDependency
	ShnShopifyConnectionConsoleHelper shopifyConnectionHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shnShopifyProductSubCategorySynchronisation")
	ComponentProvider <ShnShopifySynchronisation <
		?,
		ShnProductRec,
		ShopifyCollectResponse
	>> productSubCategorySynchronisationProvider;

	@PrototypeDependency
	@NamedDependency ("shnShopifyProductSynchronisation")
	ComponentProvider <ShnShopifySynchronisation <
		?,
		ShnProductRec,
		ShopifyProductResponse
	>> productSynchronisationProvider;

	@PrototypeDependency
	@NamedDependency ("shnShopifySubCategorySynchronisation")
	ComponentProvider <ShnShopifySynchronisation <
		?,
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionResponse
	>> subCategorySynchronisationProvider;

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

				.create (
					true)

				.update (
					true)

				.remove (
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

			ShnShopifyConnectionRec shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			ShopifyApiClientCredentials shopifyCredentials =
				shopifyLogic.getApiCredentials (
					transaction,
					shopifyConnection.getStore ());

			Long remainingOperations =
				formState.maxOperations ();

			Long numOperations = 0l;

			// synchronise items

			List <ShnShopifySynchronisation <?, ?, ?>> synchronisations =
				ImmutableList.of (

				subCategorySynchronisationProvider.provide (
					transaction),

				productSynchronisationProvider.provide (
					transaction),

				productSubCategorySynchronisationProvider.provide (
					transaction)

			);

			for (
				ShnShopifySynchronisation <?, ?, ?> synchronisation
					: synchronisations
			) {

				synchronisation

					.enableCreate (
						formState.create ())

					.enableUpdate (
						formState.update ())

					.enableRemove (
						formState.remove ())

					.maxOperations (
						remainingOperations)

					.shopifyConnection (
						shopifyConnection)

					.shopifyCredentials (
						shopifyCredentials)

					.synchronise (
						transaction)

				;

				remainingOperations = sum (
					remainingOperations,
					- synchronisation.numCreated (),
					- synchronisation.numUpdated (),
					- synchronisation.numRemoved ());

				numOperations +=
					synchronisation.numOperations ();

			}

			// commit and return

			transaction.commit ();

			for (
				ShnShopifySynchronisation <?, ?, ?> synchronisation
					: synchronisations
			) {

				addNotices (
					transaction,
					synchronisation);

			}

			if (
				moreThan (
					numOperations,
					formState.maxOperations ())
			) {

				requestContext.addWarningFormat (
					"Only performed %s out of %s operations, ",
					integerToDecimalString (
						formState.maxOperations ()),
					integerToDecimalString (
						numOperations),
					"%s operations remaining",
					integerToDecimalString (
						remainingOperations));

			}

			return optionalAbsent ();

		}

	}

	// private implementation

	private
	void addNotices (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifySynchronisation <?, ?, ?> synchronisation) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addNotices");

		) {

			if (
				moreThanZero (
					synchronisation.numRemoved ())
			) {

				requestContext.addNoticeFormat (
					"%s removed",
					pluralise (
						synchronisation.numRemoved (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numNotRemoved ())
			) {

				requestContext.addWarningFormat (
					"%s not removed",
					pluralise (
						synchronisation.numNotRemoved (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numCreated ())
			) {

				requestContext.addNoticeFormat (
					"%s created",
					pluralise (
						synchronisation.numCreated (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numNotCreated ())
			) {

				requestContext.addWarningFormat (
					"%s not created",
					pluralise (
						synchronisation.numNotCreated (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numUpdated ())
			) {

				requestContext.addNoticeFormat (
					"%s updated",
					pluralise (
						synchronisation.numUpdated (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numNotUpdated ())
			) {

				requestContext.addWarningFormat (
					"%s not updated",
					pluralise (
						synchronisation.numNotUpdated (),
						synchronisation.friendlyNameSingular (),
						synchronisation.friendlyNamePlural ()));

			}

			if (
				moreThanZero (
					synchronisation.numErrors ())
			) {

				requestContext.addErrorFormat (
					"%s data mismatch errors creating or updating %s",
					integerToDecimalString (
						synchronisation.numErrors ()),
					synchronisation.friendlyNamePlural ());

			}

		}

	}

}
