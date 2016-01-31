package wbs.platform.currency.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

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

	@Inject
	SliceObjectHelper sliceHelper;

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

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"gbp")

			.setName (
				"GBP")

			.setDescription (
				"Pounds sterling")

			.setDivisions (
				100l)

			.setPrefix (
				"£")

			.setSingularSuffix (
				"")

			.setPluralSuffix (
				"")

		);

		currencyHelper.insert (
			currencyHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"usd")

			.setName (
				"USD")

			.setDescription (
				"US Dollars")

			.setDivisions (
				100l)

			.setPrefix (
				"$")

			.setSingularSuffix (
				"")

			.setPluralSuffix (
				"")

		);

		currencyHelper.insert (
			currencyHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"credit")

			.setName (
				"Credits")

			.setDescription (
				"Arbitrary credits")

			.setDivisions (
				1l)

			.setPrefix (
				"")

			.setSingularSuffix (
				" credit")

			.setPluralSuffix (
				" credits")

		);

	}

}
