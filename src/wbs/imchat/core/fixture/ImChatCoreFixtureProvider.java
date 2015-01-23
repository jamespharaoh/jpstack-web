

package wbs.imchat.core.fixture;

import java.util.Random;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.imchat.core.model.ImChatConversationObjectHelper;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatRec;
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
	SliceObjectHelper sliceHelper;

	// implementation
	
	@Inject
	Random random;

	@Override
	public
	void createFixtures () {

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

		ImChatRec imChat =
			imChatHelper.insert (
				new ImChatRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"im_chat")

			.setName (
				"im_chat")

			.setDescription (
				"im_chat")

		);

		ImChatCustomerRec imChatCustomer =
			imChatCustomerHelper.insert (
				new ImChatCustomerRec ()

			.setImChat (
				imChat)

			.setCode (
				generateCode ())

		);

		ImChatConversationRec imChatConversation =
			imChatConversationHelper.insert (
				new ImChatConversationRec ()

			.setImChatCustomer (
				imChatCustomer)

			.setIndex (
				imChatCustomer.getNumConversations ())

		);

		imChatCustomer

			.setNumConversations (
				imChatCustomer.getNumConversations () + 1);

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
	
	public
	String generateCode () {

		int code;
		
		code = random.nextInt (90000000) + 10000000;

		return Integer.toString (code);

	}

}

