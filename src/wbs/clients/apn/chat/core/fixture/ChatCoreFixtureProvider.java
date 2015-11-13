package wbs.clients.apn.chat.core.fixture;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("chatCoreFixtureProvider")
public
class ChatCoreFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CurrencyObjectHelper currencyHelper;

	/*
	@Inject
	TicketManagerObjectHelper ticketManagerHelper;
	*/

	@Inject
	Database database;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	RandomLogic randomLogic;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		Transaction transaction =
			database.currentTransaction ();

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
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

		// routes

		RouteRec billRoute =
			routeHelper.insert (
				routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"chat_5_00")

			.setName (
				"Chat £5.00")

			.setDescription (
				"Chat billed £1")

			.setNumber (
				"c500")

			.setOutCharge (
				500)

			.setCanSend (
				true)

			.setCanReceive (
				true)

			.setDeliveryReports (
				true)

			.setSender (
				senderHelper.findByCode (
					GlobalId.root,
					"simulator"))

		);

		RouteRec freeRoute =
			routeHelper.insert (
				routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
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
				senderHelper.findByCode (
					GlobalId.root,
					"simulator"))

		);

		RouterRec freeRouter =
			objectManager.findChildByCode (
				RouterRec.class,
				freeRoute,
				"static");

		CommandRec magicCommand =
			commandHelper.findByCode (
				GlobalId.root,
				"magic_number");

		routeHelper.insert (
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

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
				chatHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test")

			.setCurrency (
				currencyHelper.findByCode (
					GlobalId.root,
					"gbp"))

			/* TODO should not reference by id
			.setTicketManager (
				ticketManagerHelper.find (
					1))
			*/

		);

		// templates

		chatHelpTemplateHelper.insert (
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
				billRoute)

			.setRbFreeRouter (
				freeRouter)

			.setMagicRouter (
				freeRouter)

			.setWapRouter (
				freeRouter)

		);

		chatSchemeHelper.insert (
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
				billRoute)

			.setRbFreeRouter (
				freeRouter)

			.setMagicRouter (
				freeRouter)

			.setWapRouter (
				freeRouter)

		);

		// chat users, regular

		List<ChatUserRec> chatUsers =
			new ArrayList<ChatUserRec> ();

		for (int index = 0; index < 260; index ++) {

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
					numberHelper.createInstance ()

				.setNumber (
					numberString)

				.setNetwork (
					networkHelper.findByCode (
						GlobalId.root,
						"unknown"))

			);

			ChatUserRec chatUser =
				chatUserHelper.insert (
					chatUserHelper.createInstance ()

				.setChat (
					chat)

				.setCode (
					code)

				.setCreated (
					instantToDate (
						transaction.now ()))

				.setChatScheme (
					leftChatScheme)

				.setType (
					ChatUserType.user)

				.setNumber (
					number)

				.setCreditMode (
					ChatUserCreditMode.strict)

			);

			chatUsers.add (
				chatUser);

		}

		// chat users, monitors

		List<ChatUserRec> chatMonitors =
			new ArrayList<ChatUserRec> ();

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
					chatUserHelper.createInstance ()

				.setChat (
					chat)

				.setCode (
					code)

				.setCreated (
					instantToDate (
						transaction.now ()))

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
				textHelper.findOrCreate (
					stringFormat (
						"Chat message user to user %s",
						index));

			chatMessageHelper.insert (
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
					instantToDate (
						pastInstant (
							transaction.now ())))

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
				textHelper.findOrCreate (
					stringFormat (
						"Chat message to monitor %s",
						index));

			chatMessageHelper.insert (
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
					instantToDate (
						pastInstant (
							transaction.now ())))

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
				textHelper.findOrCreate (
					stringFormat (
						"Chat message from monitor %s",
						index));

			chatMessageHelper.insert (
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
					instantToDate (
						pastInstant (
							transaction.now ())))

				.setSender (
					randomLogic.sample (
						userHelper.findAll ()))

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

		int choice =
			randomLogic.randomInteger (1000);

		return now.minus (
			choice * choice);

	}

}
