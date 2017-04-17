package wbs.apn.chat.core.fixture;

import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.media.fixture.MediaTestImagesFixtureProvider;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandObjectHelper;
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
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;

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
	ChatUserImageObjectHelper chatUserImageHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	GazetteerObjectHelper gazetteerHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
	MediaLogic mediaLogic;

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

	// state

	List <NetworkRec> networks;
	List <UserRec> users;

	List <ChatSchemeRec> chatSchemes;

	List <ChatUserRec> chatUsers =
		new ArrayList<> ();

	List <ChatUserRec> chatMonitors =
		new ArrayList<> ();

	Set <String> chatUserCodes =
		new HashSet<> ();

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

		networks =
			networkHelper.findAll ();

		users =
			userHelper.findAll ();

		createMenuItems (
			taskLogger,
			transaction);

		createRoutes (
			taskLogger,
			transaction);

		createChatServices (
			taskLogger,
			transaction);

		createChatTemplates (
			taskLogger,
			transaction);

		createChatSchemes (
			taskLogger,
			transaction);

		createChatUsers (
			taskLogger,
			transaction);

		createChatMessages (
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createRoutes");

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

		routeHelper.insert (
			taskLogger,
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
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
				commandHelper.findByCodeRequired (
					GlobalId.root,
					"magic_number"))

		);

		transaction.flush ();

	}

	private
	void createChatServices (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatServices");

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

		transaction.flush ();

	}

	private
	void createChatTemplates (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatTemplates");

		chatHelpTemplateHelper.insert (
			taskLogger,
			chatHelpTemplateHelper.createInstance ()

			.setChat (
				chatHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"test"))

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
				chatHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"test"))

			.setType (
				"system")

			.setCode (
				"spend_warning_2")

			.setDescription (
				"Spend warning 2")

			.setText (
				"spend 2")

		);

		transaction.flush ();

	}

	private
	void createChatSchemes (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatSchemes");

		chatSchemeHelper.insert (
			taskLogger,
			chatSchemeHelper.createInstance ()

			.setChat (
				chatHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"test"))

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
				chatHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"test"))

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

		transaction.flush ();

		chatSchemes =
			chatSchemeHelper.findAll ();

	}

	private
	void createChatUsers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatUsers");

		List <Optional <MediaRec>> imageMedias =
			ImmutableList.<Optional <MediaRec>> builder ()

			.addAll (
				iterableMap (
					mediaId ->
						optionalOf (
							mediaHelper.findRequired (
								mediaId)),
					MediaTestImagesFixtureProvider.testMediaIdsByName.values ()))

			.addAll (
				iterableMap (
					mediaId ->
						optionalAbsent (),
					MediaTestImagesFixtureProvider.testMediaIdsByName.values ()))

			.build ();

		for (
			int index = 0;
			index < 260;
			index ++
		) {

			String code =
				chatUserCode ();

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
						networks))

			);

			Gender gender =
				randomLogic.sample (
					Gender.values ());

			String name =
				randomLogic.sample (
					namesByGender.get (
						gender));

			ChatUserRec chatUser =
				chatUserHelper.insert (
					taskLogger,
					chatUserHelper.createInstance ()

				.setChat (
					chatHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"test"))

				.setCode (
					code)

				.setCreated (
					transaction.now ())

				.setChatScheme (
					randomLogic.sample (
						chatSchemes))

				.setType (
					ChatUserType.user)

				.setNumber (
					number)

				.setOldNumber (
					number)

				.setGender (
					gender)

				.setOrient (
					randomLogic.sample (
						Orient.values ()))

				.setName (
					name)

				.setInfoText (
					textHelper.findOrCreateFormat (
						taskLogger,
						"Info for %s",
						name))

				.setCreditMode (
					ChatUserCreditMode.billedMessages)

			);

			chatUserAddImage (
				taskLogger,
				transaction,
				chatUser,
				imageMedias);

			chatUsers.add (
				chatUser);

		}

		for (
			int index = 0;
			index < 260;
			index ++
		) {

			String code =
				chatUserCode ();

			Gender gender =
				randomLogic.sample (
					Gender.values ());

			String name =
				randomLogic.sample (
					namesByGender.get (
						gender));

			ChatUserRec chatUser =
				chatUserHelper.insert (
					taskLogger,
					chatUserHelper.createInstance ()

				.setChat (
					chatHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"test"))

				.setCode (
					code)

				.setCreated (
					transaction.now ())

				.setType (
					ChatUserType.monitor)

				.setGender (
					gender)

				.setOrient (
					randomLogic.sample (
						Orient.values ()))

				.setName (
					name)

				.setInfoText (
					textHelper.findOrCreateFormat (
						taskLogger,
						"Info for %s",
						name))

				.setCreditMode (
					ChatUserCreditMode.free)

			);

			chatUserAddImage (
				taskLogger,
				transaction,
				chatUser,
				imageMedias);

			chatMonitors.add (
				chatUser);

		}

		transaction.flush ();

	}

	private
	String chatUserCode () {

		for (;;) {

			String code =
				randomLogic.generateNumericNoZero (6);

			if (
				contains (
					chatUserCodes,
					code)
			) {
				continue;
			}

			chatUserCodes.add (
				code);

			return code;

		}

	}

	private
	void chatUserAddImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull ChatUserRec chatUser,
			@NonNull List <Optional <MediaRec>> imageMedias) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"chatUserAddImage");

		Optional <MediaRec> mediaOptional =
			randomLogic.sample (
				imageMedias);

		if (
			optionalIsNotPresent (
				mediaOptional)
		) {
			return;
		}

		ChatUserImageRec chatUserImage =
			chatUserImageHelper.insert (
				taskLogger,
				chatUserImageHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setType (
				ChatUserImageType.image)

			.setIndex (
				0l)

			.setStatus (
				ChatUserInfoStatus.console)

			.setTimestamp (
				transaction.now ())

			.setFullMedia (
				mediaOptional.get ())

			.setMedia (
				mediaOptional.get ())

		);

		chatUser.setMainChatUserImage (
			chatUserImage);

		chatUser.getChatUserImages ().add (
			chatUserImage);

		chatUser.getChatUserImageList ().add (
			chatUserImage);

	}

	private
	void createChatMessages (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatMessages");

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
					chatHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"test"))

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

			if (index % 100 == 0) {
				transaction.flush ();
			}

		}

		transaction.flush ();

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
					chatHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"test"))

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

			if (index % 100 == 0) {
				transaction.flush ();
			}

		}

		transaction.flush ();

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
					chatHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"test"))

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
						users))

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
				transaction.flush ();
			}

		}

		transaction.flush ();

	}

	Instant pastInstant (
			Instant now) {

		long choice =
			randomLogic.randomInteger (
				1000l);

		return now.minus (
			choice * choice);

	}

	// constants

	private final static
	List <String> maleNames =
		ImmutableList.of (
			"Alfonso", "Arnoldo", "Arthur", "Bobbie", "Britt", "Cristobal",
			"Cruz", "Damon", "Deshawn", "Dominick", "Donn", "Emil", "Erwin",
			"Floyd", "Fredric", "Garfield", "Gayle", "Gerard", "Grover",
			"Harland", "Harold", "Homer", "Hunter", "Ike", "Jarred",
			"Kristofer", "Lane", "Leif", "Lindsay", "Lupe", "Luther", "Lynn",
			"Milford", "Myles", "Normand", "Oliver", "Reinaldo", "Rex", "Ron",
			"Roy", "Sandy", "Sanford", "Stan", "Stanton", "Thaddeus", "Timmy",
			"Victor", "Ward", "Wilbur", "Zachariah");

	private final static
	List <String> femaleNames =
		ImmutableList.of (
			"Adah", "Alesia", "Alissa", "Aiko", "Aracely", "Blondell",
			"Brianna", "Carmela", "Chelsey", "Concetta", "Consuelo", "Darla",
			"Deena", "Dianna", "Evonne", "Exie", "Felipa", "Gloria", "Hallie",
			"Herlinda", "Inocencia", "Keely", "Latoyia", "Lavonda", "Lecia",
			"Lenita", "Lina", "Louann", "Maranda", "Marguerite", "Marian",
			"May", "Maybell", "Melaine", "Nila", "Nola", "Olga", "Ona",
			"Ressie", "Rheba", "Sally", "Sasha", "Sharonda", "Suzette", "Talia",
			"Tamesha", "Telma", "Violeta", "Vonnie", "Yuko");

	private final static
	Map <Gender, List <String>> namesByGender =
		ImmutableMap.<Gender, List <String>> builder ()

		.put (
			Gender.male,
			maleNames)

		.put (
			Gender.female,
			femaleNames)

		.build ();

}
