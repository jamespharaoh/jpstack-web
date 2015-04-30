package wbs.services.messagetemplate.fixture;

import javax.inject.Inject;

import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeCharset;
import wbs.services.messagetemplate.model.MessageTemplateTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateValueRec;

public class MessageTemplateFixtureProvider
	implements FixtureProvider {
		
	// dependencies
	
	@Inject
	MenuGroupObjectHelper menuGroupHelper;
	
	@Inject
	MenuItemObjectHelper menuHelper;
	
	@Inject
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;
	
	@Inject
	MessageTemplateTypeObjectHelper messageTemplateTypeHelper;
	
	@Inject
	MessageTemplateValueObjectHelper messageTemplateValueHelper;
	
	@Inject
	MessageTemplateSetObjectHelper messageTemplateSetHelper;
	
	@Inject
	ObjectTypeObjectHelper objectTypeHelper;
	
	@Inject
	ObjectManager objectManager;
	
	@Inject
	SliceObjectHelper sliceHelper;
	
	@Inject
	RandomLogic randomLogic;
	
	// implementation
	
	@Override
	public
	void createFixtures () {
	
		menuHelper.insert (
			new MenuItemRec ()
	
				.setMenuGroup (
					menuGroupHelper.findByCode (
						GlobalId.root,
						"test",
						"facility"))
		
				.setCode (
					"message_template_database")
					
				.setName (
					"Message Template Database")
		
				.setDescription (
					"Message Template Database description")
		
				.setLabel (
					"Message Template Database")
		
				.setTargetPath (
					"/messageTemplateDatabases")
		
				.setTargetFrame (
					"main")
	
		);
	
		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateDatabaseHelper.insert (
				new MessageTemplateDatabaseRec ()
	
					.setSlice (
						sliceHelper.findByCode (
							GlobalId.root,
							"test"))
			
					.setCode (
						"message_template_database")
					
					.setName (
						"My message template database")
					
					.setDescription (
						"Message template database description")
	
		);
		
		MessageTemplateTypeRec messageTemplateType =
			messageTemplateTypeHelper.insert (
				new MessageTemplateTypeRec ()
		
					.setMessageTemplateDatabase (
						messageTemplateDatabase)
							
					.setName("MessageTemplateType")
					
					.setDefaultValue (
						"Default Value")
						
					.setHelpText (
						"<p>Help text</p>")
		
					.setMinLength(
						1)
	
					.setMaxLength(
						10)
						
					.setCharset (
						MessageTemplateTypeCharset.unicode)
			
		);
		
		messageTemplateDatabase
			.getMessageTemplateTypes().add (
				messageTemplateType);
		
		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetHelper.insert (
				new MessageTemplateSetRec ()
			
					.setMessageTemplateDatabase (
						messageTemplateDatabase)
							
					.setName("MessageTemplateSet")
					
					.setCode (
						"messageTemplateSet")
						
					.setDescription (
						"Message template set description")
		);
		
		MessageTemplateValueRec messageTemplateValue =
			messageTemplateValueHelper.insert(
				new MessageTemplateValueRec ()
					
					.setMessageTemplateSet (
							messageTemplateSet)
			
					.setMessageTemplateType (
						messageTemplateType)
						
					.setStringValue(
						"Message Template Value")
					
		);
		
		messageTemplateSet
			.getMessageTemplateValues().add (
				messageTemplateValue);
	
	}

}
