package wbs.platform.currency.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;

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
	MenuObjectHelper menuHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"system"))

			.setCode (
				"currency")

			.setLabel (
				"Currencies")

			.setPath (
				"/currencys")

		);

		currencyHelper.insert (
			new CurrencyRec ()

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
