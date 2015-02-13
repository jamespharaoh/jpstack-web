package wbs.paypal.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.paypal.model.PaypalAccountObjectHelper;
import wbs.paypal.model.PaypalAccountRec;
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

	//@Inject
	//PaypalPaymentObjectHelper paypalPaymentHelper;

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
					"facility"))

			.setCode (
				"paypal_account")

			.setLabel (
				"Paypal accounts")

			.setPath (
				"/paypalAccounts")

		);

		/*paypalAccountHelper.insert (
			new PaypalAccountRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test paypal account")

		);*/

	}

}
