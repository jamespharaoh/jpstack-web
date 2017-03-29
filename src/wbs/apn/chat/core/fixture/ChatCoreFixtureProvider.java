package wbs.apn.chat.core.fixture;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gazetteer.model.GazetteerObjectHelper;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.sms.route.sender.model.SenderObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatCoreFixtureProvider")
public
class ChatCoreFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatSchemeObjectHelper chatSchemeHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	GazetteerObjectHelper gazetteerHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

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

		createRoutes (
			taskLogger);

		createChatServices (
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
				"chat")

			.setName (
				"Chat")

			.setDescription (
				"Chat")

			.setLabel (
				"Chat")

			.setTargetPath (
				"/chats")

			.setTargetFrame (
				"main")

		);

	}

	private
	void createRoutes (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createRoutes");

		// routes

		routeHelper.insert (
			taskLogger,
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"chat_5_00")

			.setName (
				"Chat £5.00")

			.setDescription (
				"Chat billed £5.00")

			.setNumber (
				"c500")

			.setOutCharge (
				500l)

			.setCanSend (
				true)

			.setCanReceive (
				true)

			.setDeliveryReports (
				true)

			.setSender (
				senderHelper.findByCodeRequired (
					GlobalId.root,
					"simulator"))

		);

		routeHelper.insert (
			taskLogger,
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"chat_free")

			.setName (
				"Chat free")

			.setDescription (
				"Chat free")

			.setCanSend (
				true)

			.setDeliveryReports (
				true)

			.setSender (
				senderHelper.findByCodeRequired (
					GlobalId.root,
					"simulator"))

		);

		database.flush ();

	}

	private
	void createChatServices (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatServices");

		Transaction transaction =
			database.currentTransaction ();

		List<NetworkRec> allNetworks =
			networkHelper.findAll ();

		List<UserRec> allUsers =
			userHelper.findAll ();

		SliceRec testSlice =
			sliceHelper.findByCodeRequired (
				GlobalId.root,
				"test");

		CommandRec magicCommand =
			commandHelper.findByCodeRequired (
				GlobalId.root,
				"magic_number");

		routeHelper.insert (
			taskLogger,
			routeHelper.createInstance ()

			.setSlice (
				testSlice)

			.setCode (
				"chat_magic")

			.setName (
				"Chat magic")

			.setDescription (
				"Chat magic")

			.setCanReceive (
				true)

			.setCommand (
				magicCommand)

		);

		// chat

		ChatRec chat =
			chatHelper.insert (
				taskLogger,
				chatHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test")

			.setCurrency (
				currencyHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"gbp"))

			.setGazetteer (
				gazetteerHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"test"))

			/* TODO should not reference by id
			.setTicketManager (
				ticketManagerHelper.find (
					1))
			*/

		);

		// templates

		chatHelpTemplateHelper.insert (
			taskLogger,
			chatHelpTemplateHelper.createInstance ()

			.setChat (
				chat)

			.setType (
				"system")

			.setCode (
				"spend_warning_1")

			.setDescription (
				"Spend warning 1")

			.setText (
				"spend 1")

		);

		chatHelpTemplateHelper.insert (
			taskLogger,
			chatHelpTemplateHelper.createInstance ()

			.setChat (
				chat)

			.setType (
				"system")

			.setCode (
				"spend_warning_2")

			.setDescription (
				"Spend warning 2")

			.setText (
				"spend 2")

		);

		// chat schemes

		ChatSchemeRec leftChatScheme =
			chatSchemeHelper.insert (
				taskLogger,
				chatSchemeHelper.createInstance ()

			.setChat (
				chat)

			.setCode (
				"left")

			.setName (
				"Left")

			.setDescription (
				"Left")

			.setRbBillRoute (
				routeHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"chat_5_00"))

			.setRbFreeRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

			.setMagicRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

			.setWapRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

		);

		chatSchemeHelper.insert (
			taskLogger,
			chatSchemeHelper.createInstance ()

			.setChat (
				chat)

			.setCode (
				"right")

			.setName (
				"Right")

			.setDescription (
				"Right")

			.setRbBillRoute (
				routeHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"chat_5_00"))

			.setRbFreeRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

			.setMagicRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

			.setWapRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"chat_free"),
					"static"))

		);

		// chat users, regular

		List <ChatUserRec> chatUsers =
			new ArrayList<> ();

		for (
			int index = 0;
			index < 260;
			index ++
		) {

			String code =
				String.format (
					"%06d",
					index + 100000);

			String numberString =
				String.format (
					"447979%s",
					code);

			NumberRec number =
				numberHelper.insert (
					taskLogger,
					numberHelper.createInstance ()

				.setNumber (
					numberString)

				.setNetwork (
					randomLogic.sample (
						allNetworks))

			);

			ChatUserRec chatUser =
				chatUserHelper.insert (
					taskLogger,
					chatUserHelper.createInstance ()

				.setChat (
					chat)

				.setCode (
					code)

				.setCreated (
					transaction.now ())

				.setChatScheme (
					leftChatScheme)

				.setType (
					ChatUserType.user)

				.setNumber (
					number)

				.setCreditMode (
					ChatUserCreditMode.billedMessages)

			);

			chatUsers.add (
				chatUser);

		}

		// chat users, monitors

		List <ChatUserRec> chatMonitors =
			new ArrayList<> ();

		for (
			int index = 0;
			index < 260;
			index ++
		) {

			String code =
				String.format (
					"%06d",
					index + 200000);

			ChatUserRec chatUser =
				chatUserHelper.insert (
					taskLogger,
					chatUserHelper.createInstance ()

				.setChat (
					chat)

				.setCode (
					code)

				.setCreated (
					transaction.now ())

				.setType (
					ChatUserType.monitor)

				.setCreditMode (
					ChatUserCreditMode.free)

			);

			chatMonitors.add (
				chatUser);

		}

		database.flush ();

		// chat messages, between users

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreateFormat (
					taskLogger,
					"Chat message user to user %s",
					integerToDecimalString (
						index));

			chatMessageHelper.insert (
				taskLogger,
				chatMessageHelper.createInstance ()

				.setChat (
					chat)

				.setFromUser (
					randomLogic.sample (
						chatUsers))

				.setToUser (
					randomLogic.sample (
						chatUsers))

				.setTimestamp (
					pastInstant (
						transaction.now ()))

				.setSource (
					ChatMessageMethod.api)

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0)
				database.flush ();

		}

		database.flush ();

		// chat messages, to monitors

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreateFormat (
					taskLogger,
					"Chat message to monitor %s",
					integerToDecimalString (
						index));

			chatMessageHelper.insert (
				taskLogger,
				chatMessageHelper.createInstance ()

				.setChat (
					chat)

				.setFromUser (
					randomLogic.sample (
						chatUsers))

				.setToUser (
					randomLogic.sample (
						chatMonitors))

				.setTimestamp (
					pastInstant (
						transaction.now ()))

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0)
				database.flush ();

		}

		database.flush ();

		// chat messages, from monitors

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreateFormat (
					taskLogger,
					"Chat message from monitor %s",
					integerToDecimalString (
						index));

			chatMessageHelper.insert (
				taskLogger,
				chatMessageHelper.createInstance ()

				.setChat (
					chat)

				.setFromUser (
					randomLogic.sample (
						chatMonitors))

				.setToUser (
					randomLogic.sample (
						chatUsers))

				.setTimestamp (
					pastInstant (
						transaction.now ()))

				.setSender (
					randomLogic.sample (
						allUsers))

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0) {
				database.flush ();
			}

		}

	}

	Instant pastInstant (
			Instant now) {

		long choice =
			randomLogic.randomInteger (
				1000l);

		return now.minus (
			choice * choice);

	}

}
