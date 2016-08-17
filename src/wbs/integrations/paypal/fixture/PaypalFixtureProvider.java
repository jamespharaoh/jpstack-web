package wbs.integrations.paypal.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.record.GlobalId;
import wbs.integrations.paypal.model.PaypalAccountObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("paypalFixtureProvider")
public
class PaypalFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	PaypalAccountObjectHelper paypalAccountHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TestAccounts testAccounts;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"integration"))

			.setCode (
				"paypal")

			.setName (
				"Paypal")

			.setDescription (
				"")

			.setLabel (
				"Paypal")

			.setTargetPath (
				"/paypalAccounts")

			.setTargetFrame (
				"main")

		);

		testAccounts.forEach (
			"paypal",
			this::createTestAccount);

	}

	void createTestAccount (
			@NonNull Map<String,String> params) {

		paypalAccountHelper.insert (
			paypalAccountHelper.createInstance ()

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

			.setUsername (
				params.get ("username"))

			.setPassword (
				params.get ("password"))

			.setSignature (
				params.get ("signature"))

			.setAppId (
				params.get ("app-id"))

			.setServiceEndpointPaypalApi (
				"https://api-3t.sandbox.paypal.com/2.0")

			.setServiceEndpointPaypalApiAa (
				"https://api-3t.sandbox.paypal.com/2.0")

			.setServiceEndpointPermissions (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointAdaptivePayments (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointAdaptiveAccounts (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointInvoice (
				"https://svcs.sandbox.paypal.com/")

			.setCheckoutUrl (
				joinWithoutSeparator (
					"https://www.paypal.com/cgi-bin/webscr",
					"?cmd=_express-checkout&token={token}"))

			.setMode (
				"live")

		);

	}

}


