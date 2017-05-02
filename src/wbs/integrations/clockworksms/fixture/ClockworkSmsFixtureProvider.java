package wbs.integrations.clockworksms.fixture;

import static wbs.utils.etc.LogicUtils.parseBooleanYesNoRequired;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.joinWithNewline;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;

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

	// singleton dependencies

	@SingletonDependency
	ClockworkSmsConfigObjectHelper clockworkSmsConfigHelper;

	@SingletonDependency
	ClockworkSmsDeliveryStatusDetailCodeObjectHelper
	clockworkSmsDeliveryStatusDetailCodeHelper;

	@SingletonDependency
	ClockworkSmsDeliveryStatusObjectHelper clockworkSmsDeliveryStatusHelper;

	@SingletonDependency
	ClockworkSmsRouteInObjectHelper clockworkSmsRouteInHelper;

	@SingletonDependency
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TestAccounts testAccounts;

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

			createMenus (
				transaction);

			createConfig (
				transaction);

			createRoutes (
				transaction);

		}

	}

	private
	void createMenus (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenus");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
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

			transaction.flush ();

		}

	}

	private
	void createConfig (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createConfig");

		) {

			ClockworkSmsConfigRec config =
				clockworkSmsConfigHelper.insert (
					transaction,
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
					transaction,
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
					transaction,
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

			transaction.flush ();

		}

	}

	private
	void createRoutes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createRoutes");

		) {

			testAccounts.forEach (
				"clockwork-sms-route",
				testAccount ->
					createRoute (
						transaction,
						testAccount));

			transaction.flush ();

		}

	}

	private
	void createRoute (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, String> params) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createRoute");

		) {

			switch (
				params.get (
					"direction")
			) {

			case "in":

				createInboundRoute (
					transaction,
					params);

				break;

			case "out":

				createOutboundRoute (
					transaction,
					params);

				break;

			default:

				throw new RuntimeException (
					stringFormat (
						"Clockwork SMS route has invalid direction %s",
						params.get ("direction")));

			}

		}

	}

	private
	void createInboundRoute (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, String> params) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createInboundRoute");

		) {

			RouteRec smsRoute =
				smsRouteHelper.insert (
					transaction,
					smsRouteHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
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
				transaction,
				clockworkSmsRouteInHelper.createInstance ()

				.setRoute (
					smsRoute)

				.setClockworkSmsConfig (
					clockworkSmsConfigHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"default"))

			);

		}

	}

	private
	void createOutboundRoute (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, String> params) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createOutboundRoute");

		) {

			RouteRec smsRoute =
				smsRouteHelper.insert (
					transaction,
					smsRouteHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
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
						transaction,
						GlobalId.root,
						"clockwork_sms"))

			);

			clockworkSmsRouteOutHelper.insert (
				transaction,
				clockworkSmsRouteOutHelper.createInstance ()

				.setRoute (
					smsRoute)

				.setClockworkSmsConfig (
					clockworkSmsConfigHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"default"))

				.setUrl (
					params.get ("url"))

				.setKey (
					params.get ("key"))

				.setMaxParts (
					parseIntegerRequired (
						params.get ("max-parts")))

				.setSimulateMultipart (
					parseBooleanYesNoRequired (
						params.get ("simulate-multipart")))

			);

		}

	}

	public final static
	List <DefaultDeliveryStatus> defaultDeliveryStatuses =
		ImmutableList.<DefaultDeliveryStatus> builder ()

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
	List <DefaultDeliveryStatusDetailCode> defaultDeliveryStatusDetailCodes =
		ImmutableList.<DefaultDeliveryStatusDetailCode> builder ()

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
