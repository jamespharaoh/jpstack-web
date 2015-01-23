package wbs.paypal.fixture;

import java.util.Random;

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
	
	@Inject
	SliceObjectHelper sliceHelper;

	// implementation
	
	@Inject
	Random random;

	@Override
	public
	void createFixtures () {

		MenuRec menu = new MenuRec()
				.setMenuGroup (
						menuGroupHelper.findByCode (
						GlobalId.root,
						"facility"))

				.setCode (
					"paypal_account")

				.setLabel (
					"Paypal Account")

				.setPath (
					"/paypalAccounts");
		
		menuHelper.insert (menu);
		
		PaypalAccountRec paypalAccount = new PaypalAccountRec ()

			.setSlice(sliceHelper.findByCode(GlobalId.root, "test"))
	
			.setCode("paypalAccount")
			
			.setName("paypalAccount")
			.setDescription("paypalAccount"); 
					
		paypalAccountHelper.insert (paypalAccount);

	}
	
	public
	String generateCode () {

		int code;
		
		code = random.nextInt (90000000) + 10000000;

		return Integer.toString (code);

	}

}
