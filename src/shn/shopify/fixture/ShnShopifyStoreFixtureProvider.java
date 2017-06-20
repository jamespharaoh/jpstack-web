package shn.shopify.fixture;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.CodeUtils.simplifyToCodeRelaxed;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import shn.shopify.model.ShnShopifyStoreObjectHelper;

public
class ShnShopifyStoreFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnShopifyStoreObjectHelper shopifyStoreHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TestAccounts testAccounts;

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

			createMenuItems (
				transaction);

			createStores (
				transaction);

		}

	}

	// private implementation

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
						"test",
						"shopping_nation"))

				.setCode (
					"shopify")

				.setName (
					"Shopify")

				.setDescription (
					"Shopify")

				.setLabel (
					"Shopify")

				.setTargetPath (
					"/shnShopify")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createStores (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStores");

		) {

			testAccounts.forEach (
				"shopify-store",
				testAccount ->
					createStore (
						transaction,
						testAccount));

		}

	}

	private
	void createStore (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, String> params) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStore");

		) {

			shopifyStoreHelper.insert (
				transaction,
				shopifyStoreHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test"))

				.setCode (
					simplifyToCodeRelaxed (
						mapItemForKeyRequired (
							params,
							"name")))

				.setName (
					mapItemForKeyRequired (
						params,
						"name"))

				.setDescription (
					mapItemForKeyRequired (
						params,
						"name"))

				.setStoreName (
					mapItemForKeyRequired (
						params,
						"store-name"))

				.setPrivateAppName (
					mapItemForKeyRequired (
						params,
						"private-app-name"))

				.setApiKey (
					mapItemForKeyRequired (
						params,
						"api-key"))

				.setPassword (
					mapItemForKeyRequired (
						params,
						"password"))

				.setSharedSecret (
					mapItemForKeyRequired (
						params,
						"shared-secret"))

			);

		}

	}

}
