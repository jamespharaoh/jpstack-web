package wbs.integrations.clockworksms.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.StringUtils.joinWithNewline;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.lowercase;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.integrations.clockworksms.model.ClockworkSmsConfigObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsConfigRec;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusDetailCodeObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteInObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("clockworkSmsFixtureProvider")
public
class ClockworkSmsFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	ClockworkSmsConfigObjectHelper clockworkSmsConfigHelper;

	@Inject
	ClockworkSmsDeliveryStatusDetailCodeObjectHelper
	clockworkSmsDeliveryStatusDetailCodeHelper;

	@Inject
	ClockworkSmsDeliveryStatusObjectHelper clockworkSmsDeliveryStatusHelper;

	@Inject
	ClockworkSmsRouteInObjectHelper clockworkSmsRouteInHelper;

	@Inject
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RouteObjectHelper smsRouteHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TestAccounts testAccounts;

	// implementation

	@Override
	public
	void createFixtures () {

		createMenus ();

		createConfig ();

		createRoutes ();

	}

	private
	void createMenus () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"integration"))

			.setCode (
				"clockwork_sms")

			.setName (
				"Clockwork SMS")

			.setDescription (
				"")

			.setLabel (
				"Clockwork SMS")

			.setTargetPath (
				"/clockworkSms")

			.setTargetFrame (
				"main")

		);

	}

	private
	void createConfig () {

		ClockworkSmsConfigRec config =
			clockworkSmsConfigHelper.insert (
				clockworkSmsConfigHelper.createInstance ()

			.setCode (
				"default")

			.setName (
				"Default")

			.setDescription (
				"Default")

		);

		for (
			DefaultDeliveryStatus defaultDeliveryStatus
				: defaultDeliveryStatuses
		) {

			clockworkSmsDeliveryStatusHelper.insert (
				clockworkSmsDeliveryStatusHelper.createInstance ()

				.setClockworkSmsConfig (
					config)

				.setCode (
					lowercase (
						defaultDeliveryStatus.status ()))

				.setDescription (
					defaultDeliveryStatus.status ())

				.setTheirDescription (
					defaultDeliveryStatus.theirDescription ())

				.setTheirCommonCauses (
					defaultDeliveryStatus.theirCommonCauses ())

				.setMessageStatus (
					defaultDeliveryStatus.ourStatus ())

			);

		}

		for (
			DefaultDeliveryStatusDetailCode defaultDeliveryStatusDetailCode
				: defaultDeliveryStatusDetailCodes
		) {

			clockworkSmsDeliveryStatusDetailCodeHelper.insert (
				clockworkSmsDeliveryStatusDetailCodeHelper.createInstance ()

				.setClockworkSmsConfig (
					config)

				.setCode (
					Long.toString (
						defaultDeliveryStatusDetailCode.errorNumber ()))

				.setDescription (
					Long.toString (
						defaultDeliveryStatusDetailCode.errorNumber ()))

				.setTheirDescription (
					defaultDeliveryStatusDetailCode.theirDescription ())

				.setPermanent (
					defaultDeliveryStatusDetailCode.permanent ())

			);

		}

	}

	private
	void createRoutes () {

		testAccounts.forEach (
			"clockwork-sms-route",
			this::createRoute);

	}

	private
	void createRoute (
			@NonNull Map<String,String> params) {

		switch (
			params.get (
				"direction")
		) {

		case "in":

			createInboundRoute (
				params);

			break;

		case "out":

			createOutboundRoute (
				params);

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Clockwork SMS route has invalid direction %s",
					params.get ("direction")));

		}

	}

	private
	void createInboundRoute (
			@NonNull Map<String,String> params) {

		RouteRec smsRoute =
			smsRouteHelper.insert (
				smsRouteHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				simplifyToCodeRequired (
					params.get ("name")))

			.setName (
				params.get ("name"))

			.setDescription (
				params.get ("description"))

			.setCanReceive (
				true)

		);

		clockworkSmsRouteInHelper.insert (
			clockworkSmsRouteInHelper.createInstance ()

			.setRoute (
				smsRoute)

			.setClockworkSmsConfig (
				clockworkSmsConfigHelper.findByCodeRequired (
					GlobalId.root,
					"default"))

		);

	}

	private
	void createOutboundRoute (
			@NonNull Map<String,String> params) {

		RouteRec smsRoute =
			smsRouteHelper.insert (
				smsRouteHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				simplifyToCodeRequired (
					params.get ("name")))

			.setName (
				params.get ("name"))

			.setDescription (
				params.get ("description"))

			.setCanSend (
				true)

			.setDeliveryReports (
				true)

			.setSender (
				senderHelper.findByCodeRequired (
					GlobalId.root,
					"clockwork_sms"))

		);

		clockworkSmsRouteOutHelper.insert (
			clockworkSmsRouteOutHelper.createInstance ()

			.setRoute (
				smsRoute)

			.setClockworkSmsConfig (
				clockworkSmsConfigHelper.findByCodeRequired (
					GlobalId.root,
					"default"))

			.setUrl (
				params.get ("url"))

			.setKey (
				params.get ("key"))

			.setMaxParts (
				3l)

		);

	}

	public final static
	List<DefaultDeliveryStatus> defaultDeliveryStatuses =
		ImmutableList.<DefaultDeliveryStatus>builder ()

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"QUEUED")

			.theirDescription (
				"Queued for delivery to mobile networks.")

			.ourStatus (
				MessageStatus.sent))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"ENROUTE")

			.theirDescription (
				"Sent to mobile network.")

			.ourStatus (
				MessageStatus.submitted))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"DELIVRD")

			.theirDescription (
				"Delivered to destination.")

			.ourStatus (
				MessageStatus.delivered))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"EXPIRED")

			.theirDescription (
				"Message validity period has expired.")

			.theirCommonCauses (
				"Handset turned off or out of range")

			.ourStatus (
				MessageStatus.undelivered))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"DELETED")

			.theirDescription (
				"Message has been deleted.")

			.ourStatus (
				MessageStatus.undelivered))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"UNDELIV")

			.theirDescription (
				"Message could not be delivered.")

			.theirCommonCauses (
				joinWithNewline (
					"- Invalid mobile number",
					"- Error within the mobile network",
					"- Handset turned off or out of range"))

			.ourStatus (
				MessageStatus.undelivered))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"ACCEPTD")

			.theirDescription (
				"Message is in accepted state")

			.theirCommonCauses (
				joinWithSpace (
					"Message has been read manually on behalf of the",
					"subscriber by customer service"))

			.ourStatus (
				MessageStatus.delivered))

		.add (
			new DefaultDeliveryStatus ()

			.status (
				"UNKNOWN")

			.theirDescription (
				"No final delivery status received from the network.")

			.ourStatus (
				MessageStatus.sent))

		.build ();

	public final static
	List<DefaultDeliveryStatusDetailCode> defaultDeliveryStatusDetailCodes =
		ImmutableList.<DefaultDeliveryStatusDetailCode>builder ()

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				0l)

			.theirDescription (
				"No Error"))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				1l)

			.theirDescription (
				"Unknown – No details provided by network"))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				2l)

			.theirDescription (
				"Message details wrong")

			.permanent (
				true))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				3l)

			.theirDescription (
				"Operator Error")

			.permanent (
				true))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				4l)

			.theirDescription (
				"Operator Error")

			.permanent (
				false))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				5l)

			.theirDescription (
				"Absent Subscriber")

			.permanent (
				true))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				6l)

			.theirDescription (
				"Absent Subscriber")

			.permanent (
				false))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				9l)

			.theirDescription (
				"Phone Related Error")

			.permanent (
				true))

		.add (
			new DefaultDeliveryStatusDetailCode ()

			.errorNumber (
				10l)

			.theirDescription (
				"Phone Related Error")

			.permanent (
				false))

		.build ();

	@Accessors (fluent = true)
	@Data
	public static
	class DefaultDeliveryStatus {
		String status;
		String theirDescription;
		String theirCommonCauses;
		MessageStatus ourStatus;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class DefaultDeliveryStatusDetailCode {
		Long errorNumber;
		String theirDescription;
		Boolean permanent;
	}

}
