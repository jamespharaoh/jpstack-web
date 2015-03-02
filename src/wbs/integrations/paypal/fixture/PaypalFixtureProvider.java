package wbs.integrations.paypal.fixture;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.File;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.integrations.paypal.model.PaypalAccountObjectHelper;
import wbs.integrations.paypal.model.PaypalAccountRec;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("paypalFixtureProvider")
public
class PaypalFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	PaypalAccountObjectHelper paypalAccountHelper;

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
					"integration"))

			.setCode (
				"paypal")

			.setLabel (
				"Paypal")

			.setPath (
				"/paypalAccounts")

		);

		File testAccountsFile =
			new File ("conf/test-accounts.xml");

		if (! testAccountsFile.exists ())
			throw new RuntimeException ("ABC");

		if (testAccountsFile.exists ()) {

			DataFromXml dataFromXml =
				new DataFromXml ()

				.registerBuilderClasses (
					TestAccountsSpec.class,
					TestAccountSpec.class);

			TestAccountsSpec testAccounts =
				(TestAccountsSpec)
				dataFromXml.readFilename (
					"conf/test-accounts.xml");

			for (
				TestAccountSpec testAccount
					: testAccounts.accounts ()
			) {

				if (! equal (testAccount.type (), "paypal"))
					continue;

				if (! equal (testAccount.name (), "wbs-sandbox"))
					continue;

				paypalAccountHelper.insert (
					new PaypalAccountRec ()

					.setSlice (
						sliceHelper.findByCode (
							GlobalId.root,
							"test"))

					.setCode (
						"wbs_sandbox")

					.setName (
						"WBS Sandbox")

					.setDescription (
						"Test paypal account")

					.setUsername (
						testAccount.params ().get ("username"))

					.setPassword (
						testAccount.params ().get ("password"))

					.setSignature (
						testAccount.params ().get ("signature"))

					.setAppId (
						testAccount.params ().get ("app-id"))

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

				);

				break;

			}

		}

	}

}
