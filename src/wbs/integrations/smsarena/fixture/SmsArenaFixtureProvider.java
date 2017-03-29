package wbs.integrations.smsarena.fixture;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.smsarena.model.SmsArenaConfigObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaConfigRec;
import wbs.integrations.smsarena.model.SmsArenaReportCodeObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteInObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteOutObjectHelper;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("smsArenaFixtureProvider")
public
class SmsArenaFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsArenaRouteOutObjectHelper smsArenaRouteOutHelper;

	@SingletonDependency
	SmsArenaRouteInObjectHelper smsArenaRouteInHelper;

	@SingletonDependency
	SmsArenaConfigObjectHelper smsArenaConfigHelper;

	@SingletonDependency
	SmsArenaReportCodeObjectHelper smsArenaReportCodeHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

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

		menuItemHelper.insert (
			taskLogger,
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"integration"))

			.setCode (
				"sms_arena")

			.setName (
				"SmsArena")

			.setDescription (
				"")

			.setLabel (
				"Sms Arena")

			.setTargetPath (
				"/sms-arena")

			.setTargetFrame (
				"main")

		);

		RouteRec smsArenaRoute =
			routeHelper.insert (
				taskLogger,
				routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"sms_arena_route")

			.setName (
				"SMSArena route")

			.setDescription (
				"SMSArena route")

			.setCanSend (
				true)

			.setDeliveryReports (
				true)

			.setSender (
				senderHelper.findByCodeRequired (
					GlobalId.root,
					"sms_arena"))

		);

		// TODO we don't use properties files

		Properties prop =
			new Properties ();

		String propFileName =
			"conf/sms-arena-config.properties";

		try {

			InputStream inputStream =
				new FileInputStream (
					propFileName);

			prop.load (
				inputStream);

		} catch (Exception exception) {

			System.out.println("property file '" + propFileName + "' not found in the classpath: " + exception);
			return;

		}

		SmsArenaConfigRec smsArenaConfig =
			smsArenaConfigHelper.insert (
				taskLogger,
				smsArenaConfigHelper.createInstance ()

			.setCode (
				"sms_arena_config")

			.setName (
				"SMSArena config")

			.setDescription (
				"SMSArena config description")

			.setProfileId (
				Long.parseLong (
					prop.getProperty (
						"profileId")))

		);

		smsArenaRouteOutHelper.insert (
			taskLogger,
			smsArenaRouteOutHelper.createInstance ()

			.setSmsArenaConfig (
				smsArenaConfig)

			.setAuthKey (
				prop.getProperty (
					"authKey"))

			.setRoute (
				smsArenaRoute)

			.setRelayUrl (
				prop.getProperty (
					"smsUrl"))

		);

		smsArenaRouteInHelper.insert (
			taskLogger,
			smsArenaRouteInHelper.createInstance ()

			.setSmsArenaConfig (
				smsArenaConfig)

			.setRoute (
				smsArenaRoute)
		);

		smsArenaReportCodeHelper.insert (
			taskLogger,
			smsArenaReportCodeHelper.createInstance ()

			.setSmsArenaConfig (
				smsArenaConfig)

			.setCode (
				"1")

			.setDescription (
				"Delivered to phone")

			.setMessageStatus (
				MessageStatus.delivered)

			.setAdditionalInformation (
				"Delivered to phone")

		);

		smsArenaReportCodeHelper.insert (
			taskLogger,
			smsArenaReportCodeHelper.createInstance ()

			.setSmsArenaConfig (
				smsArenaConfig)

			.setCode (
				"2")

			.setDescription (
				"Undelivered to phone")

			.setMessageStatus (
				MessageStatus.undelivered)

			.setAdditionalInformation (
				"Undelivered to phone")

		);

		smsArenaReportCodeHelper.insert (
			taskLogger,
			smsArenaReportCodeHelper.createInstance ()

			.setSmsArenaConfig (
				smsArenaConfig)

			.setCode (
				"4")

			.setDescription (
				"Buffered to gateway")

			.setMessageStatus (
				MessageStatus.submitted)

			.setAdditionalInformation (
				"Buffered to gateway")

		);

	}

}
