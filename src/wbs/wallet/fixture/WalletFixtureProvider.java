package wbs.wallet.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
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
	WalletObjectHelper walletHelper;

	@Inject
	WalletServiceObjectHelper walletServiceHelper;

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
						walletServiceHelper.generateCode ())

			);

			walletHelper.insert (
					new WalletRec ()

				.setWalletServiceRec (
						walletService)

				.setCode (
						walletHelper.generateCode ())

			);
	}

}

