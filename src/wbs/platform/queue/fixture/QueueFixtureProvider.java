package wbs.platform.queue.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.feature.model.FeatureObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("queueFixtureProvider")
public
class QueueFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	FeatureObjectHelper featureObjectHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createMenuItems (
			taskLogger,
			transaction);

		createFeatures (
			taskLogger,
			transaction);

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createMenuItems");

		menuItemHelper.insert (
			taskLogger,
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"system"))

			.setCode (
				"queue")

			.setName (
				"Queue")

			.setDescription (
				"")

			.setLabel (
				"Queue")

			.setTargetPath (
				"/queues")

			.setTargetFrame (
				"main")

		);

		transaction.flush ();

	}

	private
	void createFeatures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFeatures");

		featureObjectHelper.insert (
			taskLogger,
			featureObjectHelper.createInstance ()

			.setCode (
				"queue_items_status_line")

			.setName (
				"Queue items status line")

			.setDescription (
				"Queue items status line feature")

		);

		transaction.flush ();

	}

}
