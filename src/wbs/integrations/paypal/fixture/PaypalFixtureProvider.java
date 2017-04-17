package wbs.integrations.paypal.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.paypal.model.PaypalAccountObjectHelper;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("paypalFixtureProvider")
public
class PaypalFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	PaypalAccountObjectHelper paypalAccountHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TestAccounts testAccounts;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		menuItemHelper.insert (
			taskLogger,
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
			testAccount ->
				createTestAccount (
					taskLogger,
					testAccount));

	}

	void createTestAccount (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <String, String> params) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createTestAccount");

		paypalAccountHelper.insert (
			taskLogger,
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


