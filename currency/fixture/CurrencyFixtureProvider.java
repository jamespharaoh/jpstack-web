package wbs.platform.currency.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("currencyFixtureProvider")
public
class CurrencyFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			menuItemHelper.insert (
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
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
				taskLogger,
				currencyHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
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
				taskLogger,
				currencyHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
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
				taskLogger,
				currencyHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
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

}
