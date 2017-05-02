package wbs.test.simulator.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

import wbs.test.simulator.model.SimulatorObjectHelper;
import wbs.test.simulator.model.SimulatorRec;
import wbs.test.simulator.model.SimulatorRouteObjectHelper;
import wbs.test.simulator.model.SimulatorSessionObjectHelper;

@PrototypeComponent ("simulatorFixtureProvider")
public
class SimulatorFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	SimulatorObjectHelper simulatorHelper;

	@SingletonDependency
	SimulatorRouteObjectHelper simulatorRouteHelper;

	@SingletonDependency
	SimulatorSessionObjectHelper simulatorSessionHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

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

			createMenuItem (
				transaction);

			createSimulator (
				transaction);

		}

	}

	private
	void createMenuItem (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItem");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"test"))

				.setCode (
					"simulator")

				.setName (
					"Simulator")

				.setDescription (
					"")

				.setLabel (
					"Simulator")

				.setTargetPath (
					"/simulators")

				.setTargetFrame (
					"main")

			);

		}

	}

	private
	void createSimulator (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createSimulator");

		) {

			SimulatorRec simulator =
				simulatorHelper.insert (
					transaction,
					simulatorHelper.createInstance ()

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
					"Test")

			);

			// free route

			RouteRec freeRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"free");

			freeRoute

				.setSender (
					senderHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"simulator"));

			// bill route

			RouteRec billRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"bill");

			billRoute

				.setSender (
					senderHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"simulator"));

			// magic route

			/*
			RouteRec magicRoute =
				routeHelper.findByCode (
					GlobalId.root,
					"test",
					"magic");

			simulatorRouteHelper.insert (
				new SimulatorRouteRec ()

				.setSimulator (
					simulator)

				.setPrefix (
					"magic")

				.setDescription (
					"Magic")

				.setRoute (
					magicRoute)

			);
			*/

			// inbound route

			RouteRec inboundRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"inbound");

			simulatorRouteHelper.insert (
				transaction,
				simulatorRouteHelper.createInstance ()

				.setSimulator (
					simulator)

				.setPrefix (
					"inbound")

				.setDescription (
					"Inbound")

				.setRoute (
					inboundRoute)

			);

			// session

			simulatorSessionHelper.insert (
				transaction,
				simulatorSessionHelper.createInstance ()

				.setSimulator (
					simulator)

				.setDescription (
					"Test simulator session")

				.setCreatedTime (
					transaction.now ())

				.setCreatedUser (
					userHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"test0"))

			);

		}

	}

}
