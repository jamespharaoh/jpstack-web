package wbs.imchat.core.fixture;

import java.util.Random;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.imchat.core.model.ImChatCustomerObjectHelper;
import wbs.imchat.core.model.ImChatCustomerRec;
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
		
		imChatCustomerHelper.insert(
				new ImChatCustomerRec ()

				.setImChat(imChatHelper.findByCode(imchat, "im_chat"))
		
				.setCode(generateCode()) 			
		);

	}
	
	public
	String generateCode () {

		int code;
		Random random = new Random();
		
		code = random.nextInt (90000000) + 10000000;

		return Integer.toString (code);

	}

}
