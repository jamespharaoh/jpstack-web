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

	@Override
	public
	void createFixtures () {

		MenuRec menu = new MenuRec()
				.setMenuGroup (
						menuGroupHelper.findByCode (
						GlobalId.root,
						"facility"))

				.setCode (
					"im_chat")

				.setLabel (
					"IM Chat")

				.setPath (
					"/imChats");

		menuHelper.insert (menu);

		ImChatRec imchat = new ImChatRec ()

			.setSlice(sliceHelper.findByCode(GlobalId.root, "test"))

			.setCode("im_chat")

			.setName("im_chat")
			.setDescription("im_chat");

		imChatHelper.insert (imchat);

		String code = generateCode();
		ImChatCustomerRec imchatcustomer = new ImChatCustomerRec ()

			.setImChat(imChatHelper.findByCode(imchat, "im_chat"))
			.setCode(code);

		imChatCustomerHelper.insert(imchatcustomer);

		ImChatConversationRec imchatconversation = new ImChatConversationRec ()

			.setImChatCustomer(imChatCustomerHelper.findByCode(imchatcustomer, code))
			.setIndex(imchatcustomer.getNumConversations());

		imchatcustomer.setNumConversations(imchatcustomer.getNumConversations() + 1);
		imChatConversationHelper.insert(imchatconversation);

		ImChatMessageRec imchatmessage = new ImChatMessageRec ()

			.setImChatConversation(imchatconversation)
			.setIndex(imchatconversation.getNumMessages());

		imchatconversation.setNumMessages(imchatconversation.getNumMessages() + 1);
		imChatMessageHelper.insert(imchatmessage);

	}

	public
	String generateCode () {

		int code;
		Random random = new Random();

		code = random.nextInt (90000000) + 10000000;

		return Integer.toString (code);

	}

}
