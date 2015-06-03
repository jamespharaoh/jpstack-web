package wbs.integrations.smsarena.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.integrations.smsarena.model.SmsArenaConfigObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaConfigRec;
import wbs.integrations.smsarena.model.SmsArenaRouteOutObjectHelper;
import wbs.integrations.smsarena.model.SmsArenaRouteOutRec;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("smsArenaFixtureProvider")
public class SmsArenaFixtureProvider 
	implements FixtureProvider {
	
	// dependencies
	
	@Inject
	MenuGroupObjectHelper menuGroupHelper;
	
	@Inject
	MenuItemObjectHelper menuItemHelper;
	
	@Inject
	RouteObjectHelper routeHelper;
	
	@Inject
	SmsArenaRouteOutObjectHelper smsArenaRouteOutHelper;
	
	@Inject
	SmsArenaConfigObjectHelper smsArenaConfigHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SliceObjectHelper sliceHelper;
	
	// implementation
	
	@Override
	public
	void createFixtures () {
	
		menuItemHelper.insert (
			new MenuItemRec ()
	
			.setMenuGroup (
				menuGroupHelper.findByCode (
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
				new RouteRec ()

			.setSlice (
				sliceHelper.findByCode (
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
				senderHelper.findByCode (
					GlobalId.root,
					"sms_arena"))

		);
		
		SmsArenaConfigRec smsArenaConfig =
			smsArenaConfigHelper.insert (new SmsArenaConfigRec ()
				
			.setCode("sms_arena_config")
			
			.setName("SMSArena config")

			.setDescription (
				"SMSArena config description")
				
			.setProfileId(1284)

		);

		SmsArenaRouteOutRec smsArenaRouteOut =
			smsArenaRouteOutHelper.insert (
				new SmsArenaRouteOutRec ()
				
			.setSmsArenaConfig(smsArenaConfig)

			.setAuthKey("PgAWw1ULlrJCt6g8Yc0I4QFaZ5aDtgU9")
	
			.setRoute(smsArenaRoute)
			
			.setRelayUrl("http://api.smsarena.es/http/sms.php")

		);
	
	}

}
