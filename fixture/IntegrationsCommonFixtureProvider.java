package wbs.integrations.common.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("integrationsCommonFixtureProvider")
public
class IntegrationsCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	private
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	private
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			menuGroupHelper.insert (
				transaction,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"integration")

				.setName (
					"Integration")

				.setDescription (
					"")

				.setLabel (
					"Integrations")

				.setOrder (
					60l)

			);

		}

	}

}
