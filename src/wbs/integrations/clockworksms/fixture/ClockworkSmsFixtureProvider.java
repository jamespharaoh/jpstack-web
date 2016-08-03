package wbs.integrations.clockworksms.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.record.GlobalId;
import wbs.integrations.clockworksms.model.ClockworkSmsConfigObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("clockworkSmsFixtureProvider")
public
class ClockworkSmsFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	ClockworkSmsConfigObjectHelper clockworkSmsConfigHelper;

	@Inject
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TestAccounts testAccounts;

	// implementation

	@Override
	public
	void createFixtures () {

		createMenus ();

		createConfig ();

		createRoutes ();

	}

	private
	void createMenus () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"integration"))

			.setCode (
				"clockwork_sms")

			.setName (
				"Clockwork SMS")

			.setDescription (
				"")

			.setLabel (
				"Clockwork SMS")

			.setTargetPath (
				"/clockworkSms")

			.setTargetFrame (
				"main")

		);

	}

	private
	void createConfig () {

		clockworkSmsConfigHelper.insert (
			clockworkSmsConfigHelper.createInstance ()

			.setCode (
				"default")

			.setName (
				"Default")

			.setDescription (
				"Default")

		);

	}

	private
	void createRoutes () {

		testAccounts.forEach (
			"clockwork-sms-route",
			this::createRoute);

	}

	private
	void createRoute (
			@NonNull Map<String,String> params) {

		RouteRec route =
			routeHelper.insert (
				routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				simplifyToCodeRequired (
					params.get ("name")))

			.setName (
				params.get ("name"))

			.setDescription (
				params.get ("description"))

			.setCanSend (
				true)

			.setSender (
				senderHelper.findByCodeRequired (
					GlobalId.root,
					"clockwork_sms"))

		);

		clockworkSmsRouteOutHelper.insert (
			clockworkSmsRouteOutHelper.createInstance ()

			.setRoute (
				route)

			.setClockworkSmsConfig (
				clockworkSmsConfigHelper.findByCodeRequired (
					GlobalId.root,
					"default"))

			.setUrl (
				params.get ("url"))

			.setKey (
				params.get ("key"))

		);

	}

}
