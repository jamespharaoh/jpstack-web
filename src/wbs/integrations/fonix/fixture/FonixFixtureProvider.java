package wbs.integrations.fonix.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
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

import wbs.integrations.fonix.model.FonixConfigObjectHelper;
import wbs.integrations.fonix.model.FonixDeliveryStatusObjectHelper;
import wbs.integrations.fonix.model.FonixNetworkObjectHelper;
import wbs.integrations.fonix.model.FonixRouteInObjectHelper;
import wbs.integrations.fonix.model.FonixRouteOutObjectHelper;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("fonixFixtureProvider")
public
class FonixFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	FonixConfigObjectHelper fonixConfigHelper;

	@SingletonDependency
	FonixDeliveryStatusObjectHelper fonixDeliveryStatusHelper;

	@SingletonDependency
	FonixNetworkObjectHelper fonixNetworkHelper;

	@SingletonDependency
	FonixRouteInObjectHelper fonixRouteInHelper;

	@SingletonDependency
	FonixRouteOutObjectHelper fonixRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

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

			createDefaultDeliveryStatuses (
				transaction);

			createDefaultNetworks (
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
					"fonix")

				.setName (
					"Fonix")

				.setDescription (
					"")

				.setLabel (
					"Fonix")

				.setTargetPath (
					"/fonix")

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

			fonixConfigHelper.insert (
				transaction,
				fonixConfigHelper.createInstance ()

				.setCode (
					"default")

				.setName (
					"Default")

				.setDescription (
					"Default")

			);

			transaction.flush ();

		}

	}

	private
	void createDefaultDeliveryStatuses (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createDefaultDeliveryStatuses");

		) {

			defaultDeliveryStatuses.forEach (
				defaultDeliveryStatus ->
					fonixDeliveryStatusHelper.insert (
						transaction,
						fonixDeliveryStatusHelper.createInstance ()

				.setFonixConfig (
					fonixConfigHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"default"))

				.setCode (
					defaultDeliveryStatus.code ())

				.setMessageStatus (
					defaultDeliveryStatus.status ())

				.setDescription (
					defaultDeliveryStatus.description ())

				.setPermanent (
					defaultDeliveryStatus.permanent ())

			));

			transaction.flush ();

		}

	}

	private
	void createDefaultNetworks (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createDefaultNetworks");

		) {

			defaultNetworks.forEach (
				defaultNetwork ->
					fonixNetworkHelper.insert (
						transaction,
						fonixNetworkHelper.createInstance ()

				.setFonixConfig (
					fonixConfigHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"default"))

				.setNetwork (
					networkHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						defaultNetwork.ourCode ()))

				.setTheirCode (
					defaultNetwork.theirCode ())

			));

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
				"fonix-route",
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
						"Fonix route has invalid direction %s",
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

			fonixRouteInHelper.insert (
				transaction,
				fonixRouteInHelper.createInstance ()

				.setRoute (
					smsRoute)

				.setFonixConfig (
					fonixConfigHelper.findByCodeRequired (
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

			fonixRouteOutHelper.insert (
				transaction,
				fonixRouteOutHelper.createInstance ()

				.setRoute (
					smsRoute)

				.setFonixConfig (
					fonixConfigHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"default"))

				.setUrl (
					params.get ("url"))

				.setApiKey (
					params.get ("api-key"))

			);

		}

	}

	// default data types

	@Accessors (fluent = true)
	@Data
	public static
	class DefaultDeliveryStatus {
		String code;
		MessageStatus status;
		String description;
		Boolean permanent;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class DefaultNetwork {
		String theirCode;
		String ourCode;
	}

	// defaults

	List <DefaultDeliveryStatus> defaultDeliveryStatuses =
		ImmutableList.of (

		new DefaultDeliveryStatus ()
			.code ("DELIVERED")
			.status (MessageStatus.delivered)
			.description ("MT message successfully delivered")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("INSUFFICIENT_FUNDS")
			.status (MessageStatus.undelivered)
			.description ("User didn’t have enough money")
			.permanent (false),

		new DefaultDeliveryStatus ()
			.code ("INVALID_MSISDN")
			.status (MessageStatus.undelivered)
			.description ("Mobile number not accepted by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("INVALID_OPERATOR_SERVICE")
			.status (MessageStatus.undelivered)
			.description ("Request type not accepted by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("INVALID_ORIGINATOR")
			.status (MessageStatus.undelivered)
			.description ("Originator not accepted by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("MAX_SPEND_MSISDN")
			.status (MessageStatus.undelivered)
			.description ("Daily user spend limit exceeded")
			.permanent (false),

		new DefaultDeliveryStatus ()
			.code ("OPERATOR_REJECTED")
			.status (MessageStatus.undelivered)
			.description ("Request not accepted by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("OPERATOR_TIMEOUT")
			.status (MessageStatus.reportTimedOut)
			.description ("Request not acknowledged by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("PERMANENT_OPERATOR_ERROR")
			.status (MessageStatus.undelivered)
			.description ("Request permanently failed by operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("PERMANENTLY_BARRED")
			.status (MessageStatus.undelivered)
			.description ("User is permanently barred from request type")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("SMSC_ERROR")
			.status (MessageStatus.undelivered)
			.description ("Operator SMSC encountered a processing error")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("TEMPORARY_BARRED")
			.status (MessageStatus.sent)
			.description ("User is temprorarely barred from request type")
			.permanent (false),

		new DefaultDeliveryStatus ()
			.code ("TEMPORARY_OPERATOR_ERROR")
			.status (MessageStatus.sent)
			.description ("Request temprorarely failed by operator")
			.permanent (false),

		new DefaultDeliveryStatus ()
			.code ("UNKNOWN_MSISDN")
			.status (MessageStatus.undelivered)
			.description ("Mobile number not known to operator")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("UNREACHABLE_MSISDN")
			.status (MessageStatus.sent)
			.description ("User mobile is switched off or absent")
			.permanent (false),

		new DefaultDeliveryStatus ()
			.code ("UNROUTABLE")
			.status (MessageStatus.undelivered)
			.description ("We can’t process the request due to a missing " +
				"route")
			.permanent (true),

		new DefaultDeliveryStatus ()
			.code ("UNKNOWN_ERROR")
			.status (MessageStatus.undelivered)
			.description ("Something went wrong, - the request failed")
			.permanent (true)

	);

	List <DefaultNetwork> defaultNetworks =
		ImmutableList.of (

		new DefaultNetwork ()
			.theirCode ("three-uk")
			.ourCode ("uk_three"),

		new DefaultNetwork ()
			.theirCode ("eeora-uk")
			.ourCode ("uk_orange"),

		new DefaultNetwork ()
			.theirCode ("eetmo-uk")
			.ourCode ("uk_tmobile"),

		new DefaultNetwork ()
			.theirCode ("voda-uk")
			.ourCode ("uk_vodafone"),

		new DefaultNetwork ()
			.theirCode ("o2-uk")
			.ourCode ("uk_o2"),

		new DefaultNetwork ()
			.theirCode ("virgin-uk")
			.ourCode ("uk_virgin"),

		new DefaultNetwork ()
			.theirCode ("unknown")
			.ourCode ("unknown")

	);

}
