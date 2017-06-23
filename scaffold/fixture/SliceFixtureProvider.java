package wbs.platform.scaffold.fixture;

import static wbs.utils.string.StringUtils.joinWithComma;
import static wbs.utils.string.StringUtils.underscoreToSpacesCapitalise;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("sliceFixtureProvider")
public
class SliceFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
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

			sliceHelper.insert (
				transaction,
				sliceHelper.createInstance ()

				.setCode (
					wbsConfig.defaultSlice ())

				.setName (
					underscoreToSpacesCapitalise (
						wbsConfig.defaultSlice ()))

				.setDescription (
					underscoreToSpacesCapitalise (
						wbsConfig.defaultSlice ()))

				.setSupervisorConfigNames (
					joinWithComma (
						"apn-default-test",
						"apn-all-test"))

			);

		}

	}

}
