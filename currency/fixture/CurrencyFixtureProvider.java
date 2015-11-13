package wbs.platform.currency.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("currencyFixtureProvider")
public
class CurrencyFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CurrencyObjectHelper currencyHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"system"))

			.setCode (
				"currency")

			.setName (
				"Currency")

			.setDescription (
				"")

			.setLabel (
				"Currencies")

			.setTargetPath (
				"/currencys")

			.setTargetFrame (
				"main")

		);

		currencyHelper.insert (
			currencyHelper.createInstance ()

			.setCode (
				"gbp")

			.setName (
				"GBP")

			.setDescription (
				"Pounds sterling")

			.setDivisions (
				100)

			.setPrefix (
				"£")

			.setSuffix (
				"")

		);

	}

}
