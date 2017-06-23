package shn.product.fixture;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.PlaceholderUtils.placeholderMapCurlyBraces;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.FixturesLogic;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;

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
import shn.product.model.ShnProductVariantObjectHelper;
import shn.supplier.model.ShnSupplierObjectHelper;

public
class ShnProductFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@SingletonDependency
	FixturesLogic fixturesLogic;

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
	ShnProductVariantObjectHelper productVariantHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	ShnSupplierObjectHelper supplierHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

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
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				String code =
					listItemAtIndexRequired (
						line,
						0l);

				ShnProductCategoryRec productCategory =
					productCategoryHelper.insert (
						transaction,
						productCategoryHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setDescription (
						code)

					.setPublicName (
						code)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Product",
					shnDatabase,
					productCategory,
					ImmutableMap.<String, Object> builder ()

					.put (
						"code",
						code)

					.put (
						"description",
						code)

					.put (
						"publicName",
						code)

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
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				ShnProductCategoryRec category =
					productCategoryHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						listItemAtIndexRequired (
							line,
							0l));

				String code =
					listItemAtIndexRequired (
						line,
						1l);

				ShnProductSubCategoryRec subCategory =
					productSubCategoryHelper.insert (
						transaction,
						productSubCategoryHelper.createInstance ()

					.setCategory (
						category)

					.setCode (
						code)

					.setDescription (
						code)

					.setPublicName (
						code)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Product",
					category,
					subCategory,
					ImmutableMap.<String, Object> builder ()

					.put (
						"code",
						code)

					.put (
						"description",
						code)

					.put (
						"publicName",
						code)

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
					wbsConfig.defaultSlice (),
					"test");

			for (
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productColumnNames,
						reader)
			) {

				ShnProductSubCategoryRec subCategory =
					productSubCategoryHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						mapItemForKeyRequired (
							lineMap,
							"category-code"),
						mapItemForKeyRequired (
							lineMap,
							"sub-category-code"));

				Map <String, Object> mappingHints =
					ImmutableMap.<String, Object> builder ()

					.put (
						"currency",
						shnDatabase.getCurrency ())

					.build ()

				;

				// create product

				Map <String, String> productParams =
					placeholderMapCurlyBraces (
						productColumnMap,
						fixturesLogic.placeholderFunction (
							mappingHints,
							lineMap));

				ShnProductRec product =
					eventFixtureLogic.createRecordAndEvents (
						transaction,
						"SHN Product",
						productHelper,
						subCategory,
						productParams,
						emptySet ());

				// create product variant

				Map <String, String> productVariantParams =
					placeholderMapCurlyBraces (
						productVariantColumnMap,
						fixturesLogic.placeholderFunction (
							mappingHints,
							lineMap));

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN Product",
					productVariantHelper,
					product,
					productVariantParams,
					emptySet ());

			}

			transaction.flush ();

		}

	}

	// data

	private final static
	List <String> productColumnNames =
		ImmutableList.of (
			"item-number",
			"supplier-name",
			"supplier-reference",
			"category-code",
			"sub-category-code",
			"description",
			"promo-code",
			"recommended-retail-price",
			"shopping-nation-price",
			"promo-price",
			"cost-price",
			"margin",
			"postage-and-packaging",
			"stock-quantity",
			"stock-value",
			"sold-quantity",
			"sales-retail",
			"sales-cost",
			"sales-rate",
			"last-date-aired",
			"first-date-aired",
			"active");

	private final static
	Map <String, String> productColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"item-number",
			"{item-number}")

		.put (
			"description",
			"{description}")

		.put (
			"public-title",
			"{description}")

		.put (
			"public-description",
			"{description}")

		.put (
			"supplier",
			"shn.test.{codify:supplier-name}")

		.put (
			"active",
			"yes")

		.build ()

	;

	private final static
	Map <String, String> productVariantColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"item-number",
			"{item-number}")

		.put (
			"description",
			"{description}")

		.put (
			"publicTitle",
			"{description}")

		.put (
			"supplierReference",
			"{supplier-reference}")

		.put (
			"recommendedRetailPrice",
			"{currency:recommended-retail-price}")

		.put (
			"shoppingNationPrice",
			"{currency:shopping-nation-price}")

		.put (
			"promotionalPrice",
			"{currency:promo-price}")

		.put (
			"costPrice",
			"{currency:cost-price}")

		.put (
			"postageAndPackaging",
			"{currency:postage-and-packaging}")

		.put (
			"stockQuantity",
			"{stock-quantity}")

		.put (
			"active",
			"yes")

		.build ()

	;

}
