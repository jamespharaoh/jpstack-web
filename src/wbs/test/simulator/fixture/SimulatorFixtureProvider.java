package wbs.test.simulator.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;
import wbs.test.simulator.model.SimulatorObjectHelper;
import wbs.test.simulator.model.SimulatorRec;

@PrototypeComponent ("simulatorFixtureProvider")
public
class SimulatorFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SimulatorObjectHelper simulatorHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"simulator")

			.setLabel (
				"Simulator")

			.setPath (
				"/simulators")

		);

		RouteRec freeRoute =
			routeHelper.findByCode (
				GlobalId.root,
				"test",
				"free");

		freeRoute

			.setSender (
				senderHelper.findByCode (
					GlobalId.root,
					"simulator"));

		RouteRec billRoute =
			routeHelper.findByCode (
				GlobalId.root,
				"test",
				"bill");

		billRoute

			.setSender (
				senderHelper.findByCode (
					GlobalId.root,
					"simulator"));

		simulatorHelper.insert (
			new SimulatorRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test")

		);

	}

}
