package wbs.platform.postgresql.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("postgresqlFixtureProvider")
public
class PostgresqlFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

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
					"internal"))

			.setCode (
				"postgresql")

			.setName (
				"PostgreSQL")

			.setDescription (
				"")

			.setLabel (
				"PostgreSQL")

			.setTargetPath (
				"/postgresql")

			.setTargetFrame (
				"main")

		);

	}

}
