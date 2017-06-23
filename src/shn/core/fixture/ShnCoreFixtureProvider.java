package shn.core.fixture;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;

public
class ShnCoreFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

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

			createMenuGroups (
				transaction);

			createMenuItems (
				transaction);

			createDatabase (
				transaction);

		}

	}

	// private implementation

	private
	void createMenuGroups (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuGroupHelper.insert (
				transaction,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setOrder (
					5l)

				.setCode (
					"shopping_nation")

				.setName (
					"Shopping Nation")

				.setDescription (
					"Shopping Nation")

				.setLabel (
					"Shopping Nation")

			);

			transaction.flush ();

		}

	}

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"shopping_nation"))

				.setCode (
					"database")

				.setName (
					"Database")

				.setDescription (
					"Shopping Nation databases")

				.setLabel (
					"Databases")

				.setTargetPath (
					"/shnDatabases")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createDatabase (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createDatabase");

		) {

			SliceRec slice =
				sliceHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice ());

			CurrencyRec currency =
				currencyHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"gbp");

			ShnDatabaseRec productDatabase =
				shnDatabaseHelper.insert (
					transaction,
					shnDatabaseHelper.createInstance ()

				.setSlice (
					slice)

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Test Shopping Nation database")

				.setCurrency (
					currency)

			);

			eventFixtureLogic.createEvents (
				transaction,
				"SHN Product",
				slice,
				productDatabase,
				ImmutableMap.<String, Object> builder ()

				.put (
					"code",
					"test")

				.put (
					"name",
					"Test")

				.put (
					"description",
					"Test product database")

				.put (
					"currency",
					currency)

				.build ()

			);

			transaction.flush ();

		}

	}

}
