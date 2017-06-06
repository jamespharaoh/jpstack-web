package wbs.smsapps.sendsms.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;

import wbs.smsapps.sendsms.model.SendSmsConfigObjectHelper;

@PrototypeComponent ("smsSendFixtureProvider")
public
class SendSmsFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SendSmsConfigObjectHelper sendSmsConfigHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	// public implementation

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

			createMenuItems (
				transaction);

			createSmsSend (
				transaction);

		}

	}

	// private implementation

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"facility"))

				.setCode (
					"send_sms")

				.setName (
					"Send SMS")

				.setDescription (
					"")

				.setLabel (
					"Send SMS")

				.setTargetPath (
					"/sendSmsConfigs")

				.setTargetFrame (
					"main")

			);

		}

	}

	private
	void createSmsSend (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createSmsSend");

		) {

			sendSmsConfigHelper.insert (
				transaction,
				sendSmsConfigHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test"))

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Test sms send config")

				.setSmsRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

				.setOriginator (
					"test")

			);

		}

	}

}
