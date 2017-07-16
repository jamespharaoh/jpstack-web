package shn.product.fixture;

import static wbs.utils.collection.MapUtils.emptyMap;
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

import wbs.platform.event.logic.EventFixtureLogic;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductCategoryObjectHelper;
import shn.product.model.ShnProductCategoryRec;
import shn.product.model.ShnProductSubCategoryObjectHelper;

public
class ShnProductCategoryFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@SingletonDependency
	FixturesLogic fixturesLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductCategoryObjectHelper productCategoryHelper;

	@SingletonDependency
	ShnProductSubCategoryObjectHelper productSubCategoryHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

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
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productCategoryColumnNames,
						reader)
			) {

				Map <String, String> productCategoryParams =
					placeholderMapCurlyBraces (
						productCategoryColumnMap,
						fixturesLogic.placeholderFunction (
							emptyMap (),
							lineMap));

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN product category",
					productCategoryHelper,
					shnDatabase,
					productCategoryParams,
					emptySet ());

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
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productSubCategoryColumnNames,
						reader)
			) {

				ShnProductCategoryRec category =
					productCategoryHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						mapItemForKeyRequired (
							lineMap,
							"category-code"));

				Map <String, String> productSubCategoryParams =
					placeholderMapCurlyBraces (
						productSubCategoryColumnMap,
						fixturesLogic.placeholderFunction (
							emptyMap (),
							lineMap));

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN product category",
					productSubCategoryHelper,
					category,
					productSubCategoryParams,
					emptySet ());

			}

			transaction.flush ();

		}

	}

	// data

	private final static
	List <String> productCategoryColumnNames =
		ImmutableList.of (
			"category-code",
			"description",
			"public-title");

	private final static
	List <String> productSubCategoryColumnNames =
		ImmutableList.of (
			"category-code",
			"sub-category-code",
			"description",
			"public-title");

	private final static
	Map <String, String> productCategoryColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"code",
			"{category-code}")

		.put (
			"description",
			"{description}")

		.put (
			"public-title",
			"{public-title}")

		.build ()

	;

	private final static
	Map <String, String> productSubCategoryColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"code",
			"{sub-category-code}")

		.put (
			"description",
			"{description}")

		.put (
			"publicTitle",
			"{public-title}")

		.put (
			"active",
			"yes")

		.build ()

	;

}
