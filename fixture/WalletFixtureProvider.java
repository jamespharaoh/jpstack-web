package wbs.services.wallet.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.wallet.model.WalletObjectHelper;
import wbs.services.wallet.model.WalletServiceObjectHelper;
import wbs.services.wallet.model.WalletServiceRec;

@PrototypeComponent ("walletFixtureProvider")
public
class WalletFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RandomLogic randomLogic;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	WalletObjectHelper walletHelper;

	@Inject
	WalletServiceObjectHelper walletServiceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
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
				sliceHelper.findByCodeOrNull (
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

