package wbs.test.simulator.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
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

	// dependencies

	@Inject
	Database database;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SimulatorObjectHelper simulatorHelper;

	@Inject
	SimulatorRouteObjectHelper simulatorRouteHelper;

	@Inject
	SimulatorSessionObjectHelper simulatorSessionHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		createMenuItem ();

		createSimulator ();

	}

	private
	void createMenuItem () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
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

	private
	void createSimulator () {

		Transaction transaction =
			database.currentTransaction ();

		SimulatorRec simulator =
			simulatorHelper.insert (
				simulatorHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeOrNull (
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
			routeHelper.findByCodeOrNull (
				GlobalId.root,
				"test",
				"free");

		freeRoute

			.setSender (
				senderHelper.findByCodeOrNull (
					GlobalId.root,
					"simulator"));

		// bill route

		RouteRec billRoute =
			routeHelper.findByCodeOrNull (
				GlobalId.root,
				"test",
				"bill");

		billRoute

			.setSender (
				senderHelper.findByCodeOrNull (
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
			routeHelper.findByCodeOrNull (
				GlobalId.root,
				"test",
				"inbound");

		simulatorRouteHelper.insert (
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
			simulatorSessionHelper.createInstance ()

			.setSimulator (
				simulator)

			.setDescription (
				"Test simulator session")

			.setCreatedTime (
				transaction.now ())

			.setCreatedUser (
				userHelper.findByCodeOrNull (
					GlobalId.root,
					"test",
					"test0"))

		);

	}

}
