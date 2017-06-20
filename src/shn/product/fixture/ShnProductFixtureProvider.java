package shn.product.fixture;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.web.utils.HtmlUtils.htmlEncode;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductCategoryObjectHelper;
import shn.product.model.ShnProductCategoryRec;
import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductSubCategoryObjectHelper;
import shn.product.model.ShnProductSubCategoryRec;
import shn.supplier.model.ShnSupplierObjectHelper;
import shn.supplier.model.ShnSupplierRec;

public
class ShnProductFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnProductCategoryObjectHelper productCategoryHelper;

	@SingletonDependency
	ShnProductObjectHelper productHelper;

	@SingletonDependency
	ShnProductSubCategoryObjectHelper productSubCategoryHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	ShnSupplierObjectHelper supplierHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createProductCategories (
				transaction);

			createProductSubCategories (
				transaction);

			createProducts (
				transaction);

		}

	}

	// private implementation

	private
	void createProductCategories (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createProductCategories");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-product-categories.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"test");

			for (
				List <String> line
					: iterable (
						csvReader.readAsList (
							reader))
			) {

				String name =
					listItemAtIndexRequired (
						line,
						0l);

				String code =
					simplifyToCodeRequired (
						name);

				ShnProductCategoryRec productCategory =
					productCategoryHelper.insert (
						transaction,
						productCategoryHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setName (
						name)

					.setDescription (
						name)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Product",
					shnDatabase,
					productCategory,
					ImmutableMap.<String, Object> builder ()

					.put (
						"description",
						name)

					.build ()

				);

			}

			transaction.flush ();

		}

	}

	private
	void createProductSubCategories (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createProductSubCategories");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-product-sub-categories.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"test");

			for (
				List <String> line
					: iterable (
						csvReader.readAsList (
							reader))
			) {

				ShnProductCategoryRec category =
					productCategoryHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						simplifyToCodeRequired (
							listItemAtIndexRequired (
								line,
								0l)));

				String name =
					listItemAtIndexRequired (
						line,
						1l);

				String code =
					simplifyToCodeRequired (
						name);

				ShnProductSubCategoryRec subCategory =
					productSubCategoryHelper.insert (
						transaction,
						productSubCategoryHelper.createInstance ()

					.setCategory (
						category)

					.setCode (
						code)

					.setName (
						name)

					.setDescription (
						name)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Product",
					category,
					subCategory,
					ImmutableMap.<String, Object> builder ()

					.put (
						"description",
						name)

					.build ()

				);

			}

			transaction.flush ();

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

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-products.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"test");

			for (
				List <String> line
					: iterable (
						csvReader.readAsList (
							reader))
			) {

				String itemNumber =
					listItemAtIndexRequired (
						line,
						0l);

				ShnSupplierRec supplier =
					supplierHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						simplifyToCodeRequired (
							listItemAtIndexRequired (
								line,
								1l)));

				String supplierReference =
					listItemAtIndexRequired (
						line,
						2l);

				ShnProductSubCategoryRec subCategory =
					productSubCategoryHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						simplifyToCodeRequired (
							listItemAtIndexRequired (
								line,
								3l)),
						simplifyToCodeRequired (
							listItemAtIndexRequired (
								line,
								4l)));

				String name =
					listItemAtIndexRequired (
						line,
						5l);

				@SuppressWarnings ("unused")
				String promoCode =
					listItemAtIndexRequired (
						line,
						6l);

				Optional <Long> recommendedRetailPrice =
					parseCurrencyOrEmptyString (
						shnDatabase.getCurrency (),
						listItemAtIndexRequired (
							line,
							7l));

				Optional <Long> shoppingNationPrice =
					parseCurrencyOrEmptyString (
						shnDatabase.getCurrency (),
						listItemAtIndexRequired (
							line,
							8l));

				Optional <Long> promotionalPrice =
					parseCurrencyOrEmptyString (
						shnDatabase.getCurrency (),
						listItemAtIndexRequired (
							line,
							9l));

				Optional <Long> costPrice =
					parseCurrencyOrEmptyString (
						shnDatabase.getCurrency (),
						listItemAtIndexRequired (
							line,
							10l));

				@SuppressWarnings ("unused")
				String margin =
					listItemAtIndexRequired (
						line,
						11l);

				Optional <Long> postageAndPackaging =
					parseCurrencyOrEmptyString (
						shnDatabase.getCurrency (),
						listItemAtIndexRequired (
							line,
							12l));

				Optional <Long> stockQuatity =
					parseIntegerOrEmptyString (
						listItemAtIndexRequired (
							line,
							13l));

				TextRec publicDescription =
					textHelper.findOrCreate (
						transaction,
						htmlEncode (
							name));

				ShnProductRec product =
					productHelper.insert (
						transaction,
						productHelper.createInstance ()

					.setSubCategory (
						subCategory)

					.setItemNumber (
						itemNumber)

					.setDescription (
						name)

					.setPublicTitle (
						name)

					.setPublicDescription (
						publicDescription)

					.setSupplier (
						supplier)

					.setSupplierReference (
						supplierReference)

					.setRecommendedRetailPrice (
						optionalOrNull (
							recommendedRetailPrice))

					.setShoppingNationPrice (
						optionalOrNull (
							shoppingNationPrice))

					.setPromotionalPrice (
						optionalOrNull (
							promotionalPrice))

					.setCostPrice (
						optionalOrNull (
							costPrice))

					.setPostageAndPackaging (
						optionalOrNull (
							postageAndPackaging))

					.setStockQuantity (
						optionalOr (
							stockQuatity,
							0l))

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Product",
					subCategory,
					product,
					ImmutableMap.<String, Object> builder ()

					.put (
						"description",
						name)

					.put (
						"publicTitle",
						name)

					.put (
						"publicDescription",
						publicDescription)

					.put (
						"supplier",
						supplier)

					.put (
						"supplierReference",
						supplierReference)

					.put (
						"reommendedRetailPrice",
						recommendedRetailPrice)

					.put (
						"shoppingNationPrice",
						shoppingNationPrice)

					.put (
						"promotionalPrice",
						promotionalPrice)

					.put (
						"costPrice",
						costPrice)

					.put (
						"postageAndPackaging",
						postageAndPackaging)

					.put (
						"stockQuatity",
						stockQuatity)

					.build ()

				);

			}

			transaction.flush ();

		}

	}

	private
	Optional <Long> parseCurrencyOrEmptyString (
			@NonNull CurrencyRec currency,
			@NonNull String text) {

		return optionalMapRequired (
			optionalFromNullable (
				nullIfEmptyString (
					text)),
			textAgain ->
				currencyLogic.parseTextRequired (
					currency,
					textAgain));

	}

	private
	Optional <Long> parseIntegerOrEmptyString (
			@NonNull String text) {

		return optionalMapRequired (
			optionalFromNullable (
				nullIfEmptyString (
					text)),
			textAgain ->
				parseIntegerRequired (
					textAgain));

	}

}
