package wbs.platform.user.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userFixtureProvider")
public
class UserFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	PrivObjectHelper privHelper;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserPrivObjectHelper userPrivHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		RootRec root =
			rootHelper.find (0);

		PrivRec rootManagePriv =
			privHelper.findByCode (
				root,
				"manage");

		SliceRec testSlice =
			sliceHelper.findByCode (
				root,
				"test");

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			UserRec testUser =
				userHelper.insert (
					new UserRec ()
						.setUsername ("test" + index)
						.setPassword ("qUqP5cyxm6YcTAhz05Hph5gvu9M=")
						.setActive (true)
						.setSlice (testSlice));

			userPrivHelper.insert (
				new UserPrivRec ()
					.setUser (testUser)
					.setPriv (rootManagePriv)
					.setCan (true));

		}

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"system"))

			.setCode (
				"user")

			.setLabel (
				"Users")

			.setPath (
				"/users")

		);

	}

}
