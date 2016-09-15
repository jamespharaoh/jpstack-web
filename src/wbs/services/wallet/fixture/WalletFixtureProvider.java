package wbs.services.wallet.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.wallet.model.WalletObjectHelper;
import wbs.services.wallet.model.WalletServiceObjectHelper;
import wbs.services.wallet.model.WalletServiceRec;
import wbs.utils.random.RandomLogic;

@PrototypeComponent ("walletFixtureProvider")
public
class WalletFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	WalletObjectHelper walletHelper;

	@SingletonDependency
	WalletServiceObjectHelper walletServiceHelper;

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
					"facility"))

			.setCode (
				"wallet_service")

			.setName (
				"Wallet service")

			.setDescription (
				"")

			.setLabel (
				"Wallet Service")

			.setTargetPath (
				"/walletServices")

			.setTargetFrame (
				"main")

		);

		WalletServiceRec walletService =
			walletServiceHelper.insert (
				walletServiceHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"test_wallet_service")

			.setName (
				"Test wallet service")

			.setDescription (
				"Test wallet service")

		);

		walletHelper.insert (
			walletHelper.createInstance ()

			.setWalletService (
				walletService)

			.setCode (
				randomLogic.generateNumericNoZero (8))

		);

	}

}

