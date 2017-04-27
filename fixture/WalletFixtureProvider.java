package wbs.services.wallet.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.services.wallet.model.WalletObjectHelper;
import wbs.services.wallet.model.WalletServiceObjectHelper;
import wbs.services.wallet.model.WalletServiceRec;

@PrototypeComponent ("walletFixtureProvider")
public
class WalletFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
					taskLogger,
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
				taskLogger,
				walletHelper.createInstance ()

				.setWalletService (
					walletService)

				.setCode (
					randomLogic.generateNumericNoZero (8))

			);

		}

	}

}

