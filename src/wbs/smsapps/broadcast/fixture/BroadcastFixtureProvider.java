package wbs.smsapps.broadcast.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.list.model.NumberListObjectHelper;
import wbs.sms.number.lookup.model.NumberLookupObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;

import wbs.smsapps.broadcast.model.BroadcastConfigObjectHelper;

@PrototypeComponent ("broadcastFixtureProvider")
public
class BroadcastFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	BroadcastConfigObjectHelper broadcastConfigHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberListObjectHelper numberListHelper;

	@SingletonDependency
	NumberLookupObjectHelper numberLookupHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createMenuItems (
			taskLogger);

		createBroadcastConfigs (
			taskLogger);

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

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
					"facility"))

			.setCode (
				"broadcast")

			.setName (
				"Broadcast")

			.setDescription (
				"")

			.setLabel (
				"Broadcast")

			.setTargetPath (
				"/broadcastConfigs")

			.setTargetFrame (
				"main")

		);

	}

	private
	void createBroadcastConfigs (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createBroadcastConfigs");

		broadcastConfigHelper.insert (
			taskLogger,
			broadcastConfigHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test broadcast config")

			.setRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setBlockNumberLookup (
				numberLookupHelper.findByCodeRequired (
					numberListHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"uk_blocked"),
					"default"))

			.setNumberFormat (
				numberFormatHelper.findByCodeRequired (
					GlobalId.root,
					"uk"))

		);

	}

}
