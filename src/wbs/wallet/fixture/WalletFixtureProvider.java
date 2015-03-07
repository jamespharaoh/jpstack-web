package wbs.wallet.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.wallet.model.WalletObjectHelper;
import wbs.wallet.model.WalletRec;
import wbs.wallet.model.WalletServiceObjectHelper;
import wbs.wallet.model.WalletServiceRec;

@PrototypeComponent ("walletFixtureProvider")
public
class WalletFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

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

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))

			.setCode (
				"wallet_service")

			.setLabel (
				"Wallet Service")

			.setPath (
				"/walletServices")

		);

		WalletServiceRec walletService =
			walletServiceHelper.insert (
				new WalletServiceRec ()

			.setSlice (
				sliceHelper.findByCode (
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
			new WalletRec ()

			.setWalletServiceRec (
				walletService)

			.setCode (
				randomLogic.generateNumericNoZero (8))

		);

	}

}

