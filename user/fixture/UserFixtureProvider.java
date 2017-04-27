package wbs.platform.user.fixture;

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
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userFixtureProvider")
public
class UserFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	PrivObjectHelper privHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	UserPrivObjectHelper userPrivHelper;

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

			PrivRec rootManagePriv =
				privHelper.findByCodeRequired (
					GlobalId.root,
					"manage");

			PrivRec rootDebugPriv =
				privHelper.findByCodeRequired (
					GlobalId.root,
					"debug");

			SliceRec testSlice =
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test");

			for (
				int index = 0;
				index < 10;
				index ++
			) {

				UserRec testUser =
					userHelper.insert (
						taskLogger,
						userHelper.createInstance ()

					.setUsername (
						"test" + index)

					.setPassword (
						"qUqP5cyxm6YcTAhz05Hph5gvu9M=")

					.setFullname (
						"Test " + index)

					.setDetails (
						"Test user " + index)

					.setActive (
						true)

					.setSlice (
						testSlice)

				);

				userPrivHelper.insert (
					taskLogger,
					userPrivHelper.createInstance ()

					.setUser (
						testUser)

					.setPriv (
						rootManagePriv)

					.setCan (
						true)

				);

				userPrivHelper.insert (
					taskLogger,
					userPrivHelper.createInstance ()

					.setUser (
						testUser)

					.setPriv (
						rootDebugPriv)

					.setCan (
						true)

				);

			}

			menuItemHelper.insert (
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"system"))

				.setCode (
					"user")

				.setName (
					"User")

				.setDescription (
					"")

				.setLabel (
					"Users")

				.setTargetPath (
					"/users")

				.setTargetFrame (
					"main")

			);

		}

	}

}
