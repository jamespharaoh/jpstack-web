package wbs.imchat.core.fixture;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.imchat.core.model.ImChatConversationObjectHelper;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatPricePointObjectHelper;
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatProfileObjectHelper;
import wbs.imchat.core.model.ImChatProfileRec;
import wbs.imchat.core.model.ImChatPurchaseObjectHelper;
import wbs.imchat.core.model.ImChatPurchaseRec;
import wbs.imchat.core.model.ImChatRec;
import wbs.imchat.core.model.ImChatSessionObjectHelper;
import wbs.imchat.core.model.ImChatSessionRec;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
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
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;
	
	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;
	
	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		Transaction transaction =
			database.currentTransaction ();

		// menu

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))

			.setCode (
				"im_chat")

			.setLabel (
				"IM Chat")

			.setPath (
				"/imChats")

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

			.setCurrency (
				currencyHelper.findByCode (
					GlobalId.root,
					"gbp"))

		);

		// im chat price point
		ImChatPricePointRec imChatPricePoint =
		imChatPricePointHelper.insert (
			new ImChatPricePointRec ()

			.setImChat (
				imChat)

			.setCode (
				"20_for_10")

			.setName (
				"£20 for £10")

			.setDescription (
				"£20 for £10")

			.setPrice (
				1000)

			.setValue (
				20000)

		);

		// im chat profile

		for (
			int index = 0;
			index < 10;
			index ++
		) {

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

				.setPublicName (
					stringFormat (
						"Profile %s",
						index))

				.setPublicDescription (
					stringFormat (
						"Test IM chat profile %s",
						index))

			);

		}

		// im chat customer

		ImChatCustomerRec imChatCustomer =
			imChatCustomerHelper.insert (
				new ImChatCustomerRec ()

			.setImChat (
				imChat)

			.setCode (
				imChatCustomerHelper.generateCode ())

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
				imChatSessionHelper.generateSecret ())

			.setStartTime (
				transaction.now ())
				
			.setUpdateTime (
				transaction.now ())
				
			.setEndTime (
				transaction.now ())
				
			.setActive (true)

		);
			
		// im chat purchase

			imChatPurchaseHelper.insert (
				new ImChatPurchaseRec ()

			.setImChatCustomer (
				imChatCustomer)

			.setIndex (
				imChatCustomer.getNumPurchases ())
				
			.setImChatPricePoint (
					imChatPricePoint)	

			.setPrice (
				10)
				
			.setValue (
				10)
				
			.setOldBalance (
				1)
				
			.setNewBalance (
				2)
				
			.setTimestamp (
				transaction.now ())

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

		);

		imChatConversation

			.setNumMessages (
				imChatConversation.getNumMessages () + 1);

	}

}


