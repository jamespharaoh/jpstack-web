package wbs.smsapps.broadcast.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.list.model.NumberListObjectHelper;
import wbs.sms.number.lookup.model.NumberLookupObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;

import wbs.smsapps.broadcast.model.BroadcastConfigObjectHelper;

@PrototypeComponent ("broadcastFixtureProvider")
public
class BroadcastFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	BroadcastConfigObjectHelper broadcastConfigHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberListObjectHelper numberListHelper;

	@SingletonDependency
	NumberLookupObjectHelper numberLookupHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

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

			createBroadcastConfigs (
				transaction);

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
						"test",
						"facility"))

				.setCode (
					"broadcast")

				.setName (
					"Broadcast")

				.setDescription (
					"")

				.setLabel (
					"Broadcast")

				.setTargetPath (
					"/broadcastConfigs")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createBroadcastConfigs (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createBroadcastConfigs");

		) {

			broadcastConfigHelper.insert (
				transaction,
				broadcastConfigHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test"))

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Test broadcast config")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

				.setBlockNumberLookup (
					numberLookupHelper.findByCodeRequired (
						transaction,
						numberListHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"uk_blocked"),
						"default"))

				.setNumberFormat (
					numberFormatHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"uk"))

			);

			transaction.flush ();

		}

	}

}
