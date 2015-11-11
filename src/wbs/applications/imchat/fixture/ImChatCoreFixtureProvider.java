package wbs.applications.imchat.fixture;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.SneakyThrows;

import org.apache.commons.io.IOUtils;

import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerDetailDataType;
import wbs.applications.imchat.model.ImChatCustomerDetailTypeObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatProfileState;
import wbs.applications.imchat.model.ImChatPurchaseObjectHelper;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.applications.imchat.model.ImChatTemplateObjectHelper;
import wbs.applications.imchat.model.ImChatTemplateRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.integrations.paypal.model.PaypalAccountObjectHelper;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("imChatCoreFixtureProvider")
public
class ImChatCoreFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CurrencyObjectHelper currencyHelper;

	@Inject
	Database database;

	@Inject
	ImChatCustomerDetailTypeObjectHelper imChatCustomerDetailTypeHelper;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;

	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	ImChatTemplateObjectHelper imChatTemplateHelper;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	PaypalAccountObjectHelper paypalAccountHelper;

	@Inject
	RandomLogic randomLogic;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	@SneakyThrows (Exception.class)
	public
	void createFixtures () {

		Transaction transaction =
			database.currentTransaction ();

		// menu

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"im_chat")

			.setName (
				"IM Chat")

			.setDescription (
				"Instant message chat service")

			.setLabel (
				"IM Chat")

			.setTargetPath (
				"/imChats")

			.setTargetFrame (
				"main")

		);

		// im chat

		ImChatRec imChat =
			imChatHelper.insert (
				new ImChatRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test IM chat")

			.setPaypalAccount (
				paypalAccountHelper.findByCode (
					GlobalId.root,
					"test",
					"wbs_sandbox"))

			.setCurrency (
				currencyHelper.findByCode (
					GlobalId.root,
					"gbp"))

			.setPreferredQueueTime (
				5 * 60)

			.setMessageCost (
				300)

			.setFreeMessageLimit (
				3)

			.setBillMessageMinChars (
				50)

			.setBillMessageMaxChars (
				100)

			.setFreeMessageMinChars (
				10)

			.setFreeMessageMaxChars (
				50)

			.setDevelopmentMode (
				true)

		);

		// price points

		ImChatPricePointRec basicPricePoint =
			imChatPricePointHelper.insert (
				new ImChatPricePointRec ()

			.setImChat (
				imChat)

			.setCode (
				"basic")

			.setName (
				"Basic")

			.setDescription (
				"Basic credit package")

			.setPrice (
				599)

			.setValue (
				600)

		);

		imChatPricePointHelper.insert (
			new ImChatPricePointRec ()

			.setImChat (
				imChat)

			.setCode (
				"saver")

			.setName (
				"Saver")

			.setDescription (
				"Saver credit offer")

			.setPrice (
				1099)

			.setValue (
				1200)

		);

		imChatPricePointHelper.insert (
			new ImChatPricePointRec ()

			.setImChat (
				imChat)

			.setCode (
				"super_saver")

			.setName (
				"Super saver")

			.setDescription (
				"Super saver credit offer")

			.setPrice (
				1499)

			.setValue (
				1800)

		);

		// im chat template

		for (
			int index = 0;
			index < 3;
			index ++
		) {

			imChatTemplateHelper.insert (
				new ImChatTemplateRec ()

				.setImChat (
					imChat)

				.setCode (
					stringFormat (
						"template_%s",
						index))

				.setName (
					stringFormat (
						"Template %s",
						index))

				.setDescription (
					stringFormat (
						"Test template %s",
						index))

				.setText (
					stringFormat (
						"Test IM chat template %s",
						index))

			);

		}

		// customer detail types

		imChatCustomerDetailTypeHelper.insert (
			new ImChatCustomerDetailTypeRec ()

			.setImChat (
				imChat)

			.setCode (
				"name")

			.setName (
				"Name")

			.setDescription (
				"")

			.setLabel (
				"Name")

			.setHelp (
				"Please enter your name")

			.setRequired (
				true)

			.setDataType (
				ImChatCustomerDetailDataType.text)

			.setOrdering (
				1)

		);

		imChatCustomerDetailTypeHelper.insert (
			new ImChatCustomerDetailTypeRec ()

			.setImChat (
				imChat)

			.setCode (
				"date_of_birth")

			.setName (
				"Date of birth")

			.setDescription (
				"")

			.setLabel (
				"Date of birth")

			.setHelp (
				"Please enter your date of birth")

			.setRequired (
				true)

			.setDataType (
				ImChatCustomerDetailDataType.dateOfBirth)

			.setMinimumAge (
				18)

			.setOrdering (
				2)

		);

		imChatCustomerDetailTypeHelper.insert (
			new ImChatCustomerDetailTypeRec ()

			.setImChat (
				imChat)

			.setCode (
				"star_sign")

			.setName (
				"Star sign")

			.setDescription (
				"")

			.setLabel (
				"Star sign")

			.setHelp (
				"Please enter your star sign")

			.setRequired (
				false)

			.setDataType (
				ImChatCustomerDetailDataType.chooseOne)

			.setOrdering (
				3)

		);

		// im chat profile

		MediaRec dougalMedia =
			mediaLogic.createMediaFromImage (
				IOUtils.toByteArray (
					new FileInputStream ("binaries/test/dougal.jpg")),
				"image/jpeg",
				"dougal.jpg");

		MediaRec ermintrudeMedia =
			mediaLogic.createMediaFromImage (
				IOUtils.toByteArray (
					new FileInputStream ("binaries/test/ermintrude.jpg")),
				"image/jpeg",
				"ermintrude.jpg");

		List<ImChatProfileRec> profiles =
			new ArrayList<ImChatProfileRec> ();

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			profiles.add (
				imChatProfileHelper.insert (
					new ImChatProfileRec ()

				.setImChat (
					imChat)

				.setCode (
					stringFormat (
						"profile_%s",
						index))

				.setName (
					stringFormat (
						"Profile %s",
						index))

				.setDescription (
					stringFormat (
						"Test IM chat profile %s",
						index))

				.setState (
					ImChatProfileState.ready)

				.setPublicName (
					stringFormat (
						"Profile %s",
						index))

				.setPublicDescription (
					stringFormat (
						"Test IM chat profile %s",
						index))

				.setProfileImage (
					index % 2 == 0
						? dougalMedia
						: ermintrudeMedia)
			));

		}

		// im chat customer

		ImChatCustomerRec imChatCustomer =
			imChatCustomerHelper.insert (
				new ImChatCustomerRec ()

			.setImChat (
				imChat)

			.setCode (
				randomLogic.generateNumericNoZero (8))

			.setEmail (
				"test@example.com")

			.setPassword (
				"topsecret")

		);

		// im chat conversation

		ImChatConversationRec imChatConversation =
			imChatConversationHelper.insert (
				new ImChatConversationRec ()

			.setImChatCustomer (
				imChatCustomer)

			.setIndex (
				imChatCustomer.getNumConversations ())

			.setImChatProfile (
				profiles.get (0))

			.setStartTime (
				transaction.now ())

		);

		imChatCustomer

			.setNumConversations (
				imChatCustomer.getNumConversations () + 1);

		// im chat session

		imChatSessionHelper.insert (
			new ImChatSessionRec ()

			.setImChatCustomer (
				imChatCustomer)

			.setSecret (
				randomLogic.generateLowercase (20))

			.setStartTime (
				transaction.now ())

			.setUpdateTime (
				transaction.now ())

			.setEndTime (
				transaction.now ())

			.setActive (
				true)

		);

		// im chat purchase

		imChatPurchaseHelper.insert (
			new ImChatPurchaseRec ()

			.setImChatCustomer (
				imChatCustomer)

			.setIndex (
				imChatCustomer.getNumPurchases ())

			.setImChatPricePoint (
				basicPricePoint)

			.setPrice (
				599)

			.setValue (
				600)

			.setCreatedTime (
				transaction.now ())

			.setPaypalPayment (
				null)

		);

		imChatCustomer

			.setNumPurchases (
				imChatCustomer.getNumPurchases () + 1);

		// im chat message

		imChatMessageHelper.insert (
			new ImChatMessageRec ()

			.setImChatConversation (
				imChatConversation)

			.setIndex (
				imChatConversation.getNumMessages ())

			.setMessageText (
				"Text message.")

			.setTimestamp (
				transaction.now ())

		);

		imChatConversation

			.setNumMessages (
				imChatConversation.getNumMessages () + 1);

	}

}
