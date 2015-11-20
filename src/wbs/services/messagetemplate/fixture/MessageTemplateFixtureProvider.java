package wbs.services.messagetemplate.fixture;

import javax.inject.Inject;

import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateParameterObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateParameterRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeCharset;
import wbs.services.messagetemplate.model.MessageTemplateTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateValueRec;

public
class MessageTemplateFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;

	@Inject
	MessageTemplateTypeObjectHelper messageTemplateTypeHelper;

	@Inject
	MessageTemplateParameterObjectHelper messageTemplateParameterHelper;

	@Inject
	MessageTemplateValueObjectHelper messageTemplateValueHelper;

	@Inject
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@Inject
	ImChatObjectHelper imChatHelper;

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

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

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
				messageTemplateDatabaseHelper.createInstance ()

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

		MessageTemplateTypeRec messageTemplateType1 =
			messageTemplateTypeHelper.insert (
				messageTemplateTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"template_1")

			.setName (
				"Template-1")

			.setDescription (
				"Template 1")

			.setDefaultValue (
				"Template 1 Default Value")

			.setHelpText (
				"<p>Help text 1</p>")

			.setMinLength (
				1)

			.setMaxLength (
				50)

			.setCharset (
				MessageTemplateTypeCharset.unicode)

		);

		MessageTemplateParameterRec parameterType1 =
			messageTemplateParameterHelper.insert (
				messageTemplateParameterHelper.createInstance ()

			.setCode (
				"test")

			.setName (
				"Test")

			.setMessageTemplateType (
				messageTemplateType1)

			.setRequired (
				false)

		);

		MessageTemplateTypeRec messageTemplateType2 =
			messageTemplateTypeHelper.insert (
				messageTemplateTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"template_2_parameter")

			.setName (
				"Template-2-Parameter")

			.setDescription (
				"Template 2 Parameter")

			.setDefaultValue (
				"My name is {name}")

			.setHelpText (
				"<p>Help text 2</p>")

			.setMinLength (
				5)

			.setMaxLength (
				20)

			.setCharset (
				MessageTemplateTypeCharset.unicode)

		);

		MessageTemplateParameterRec parameterType2 =
			messageTemplateParameterHelper.insert (
				messageTemplateParameterHelper.createInstance ()

			.setCode (
				"name")

			.setName (
				"Name")

			.setMessageTemplateType (
				messageTemplateType2)

			.setRequired (
				true)

			.setLength (
				4)

		);

		MessageTemplateParameterRec parameter2Type2 =
			messageTemplateParameterHelper.insert (
				messageTemplateParameterHelper.createInstance ()

			.setMessageTemplateType (
				messageTemplateType2)

			.setCode (
				"nick")

			.setName (
				"nick")

			.setRequired (
				false)

			.setLength (
				3)

		);

		MessageTemplateTypeRec messageTemplateType3 =
			messageTemplateTypeHelper.insert (
				messageTemplateTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"template_2_gsm")

			.setName (
				"Template-2-GSM")

			.setDescription (
				"Template 2 GSM")

			.setDefaultValue (
				"Va/or por d€f€c/o^")

			.setHelpText (
				"<p>Help text 3</p>")

			.setMinLength (
				5)

			.setMaxLength (
				50)

			.setCharset (
				MessageTemplateTypeCharset.gsm)

		);

		messageTemplateDatabase.getMessageTemplateTypes ().add (
			messageTemplateType1);

		messageTemplateDatabase.getMessageTemplateTypes ().add (
			messageTemplateType2);

		messageTemplateDatabase.getMessageTemplateTypes ().add (
			messageTemplateType3);

		messageTemplateType1.setNumParameters (
			messageTemplateType1.getNumParameters () + 1);

		messageTemplateType1.getMessageTemplateParameters ().add (
			parameterType1);

		messageTemplateType2.setNumParameters (
			messageTemplateType2.getNumParameters () + 2);

		messageTemplateType2.getMessageTemplateParameters ().add (
			parameterType2);

		messageTemplateType2.getMessageTemplateParameters ().add (
			parameter2Type2);

		MessageTemplateSetRec messageTemplateSet =
			messageTemplateSetHelper.insert (
				messageTemplateSetHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test message template set")

		);

		MessageTemplateValueRec messageTemplateValue =
			messageTemplateValueHelper.insert (
				messageTemplateValueHelper.createInstance ()

			.setMessageTemplateSet (
				messageTemplateSet)

			.setMessageTemplateType (
				messageTemplateType1)

			.setStringValue (
				"Message Template Value")

		);

		messageTemplateSet.setNumTemplates (
			messageTemplateSet.getNumTemplates () + 1);

		messageTemplateSet.getMessageTemplateValues ().put (
			messageTemplateType1.getId (),
			messageTemplateValue);

		/*

		///////////////////////////////////////

		// im chat message template database //

		///////////////////////////////////////

		// create database

		MessageTemplateDatabaseRec imChatMessageTemplateDatabase =
			messageTemplateDatabaseHelper.insert (
				new MessageTemplateDatabaseRec ()

					.setSlice (
						sliceHelper.findByCode (
							GlobalId.root,
							"test"))

					.setName (
						"Im Chat Message Template Database")

					.setDescription (
						"Message Template Database for Im Chat application")

		);

		// update im chat

		imChatHelper.find(1)
			.setMessageTemplateDatabase (
				imChatMessageTemplateDatabase);

		// create sets, types and values

		File imChatMessageTemplateDatabaseFile =
				new File ("conf/im-chat-message-template-database.xml");

			if (imChatMessageTemplateDatabaseFile.exists ()) {

				DataFromXml dataFromXml =
					new DataFromXml ()

					.registerBuilderClasses (
						ImChatMessageTemplateDatabaseSpec.class,
						MessagesSpec.class);

				ImChatMessageTemplateDatabaseSpec messageTemplateDatabaseElements =
					(ImChatMessageTemplateDatabaseSpec)
					dataFromXml.readFilename (
						"conf/im-chat-message-template-database.xml");

				// read xml content

				MessageTemplateSetRec imChatMessageTemplateSet =
					messageTemplateSetHelper.insert (
						new MessageTemplateSetRec ()

							.setMessageTemplateDatabase (
									imChatMessageTemplateDatabase)

							.setName("Im Chat English")

							.setDescription (
								"Im Chat message template database in english")
				);

				for (
					MessagesSpec message
						: messageTemplateDatabaseElements.messages()
				) {

					if (! equal (message.type (), "im-chat-message-template-database"))
						continue;

					if (! equal (message.name (), "english"))
						continue;

					for (
							String messageKey
								: message.params().keySet()
						) {

						MessageTemplateTypeRec imChatMessageTemplateType =
							messageTemplateTypeHelper.insert (
								new MessageTemplateTypeRec ()

									.setMessageTemplateDatabase (
										imChatMessageTemplateDatabase)

									.setName(messageKey)

									.setDefaultValue (
										message.params.get(messageKey))

									.setHelpText (
										"<p>Help text 1</p>")

									.setMinLength(
										1)

									.setMaxLength(
										500)

									.setCharset (
										MessageTemplateTypeCharset.unicode)

						);

						imChatMessageTemplateDatabase
							.getMessageTemplateTypes()
								.add(imChatMessageTemplateType);

						MessageTemplateValueRec imChatMessageTemplateValue =
								messageTemplateValueHelper.insert(
									new MessageTemplateValueRec ()

										.setMessageTemplateSet (
											messageTemplateSet)

										.setMessageTemplateType (
											imChatMessageTemplateType)

										.setStringValue(
											message.params.get(messageKey))

							);

							imChatMessageTemplateSet.setNumTemplates(
								imChatMessageTemplateSet.getNumTemplates() + 1);

							imChatMessageTemplateSet
								.getMessageTemplateValues().put (
									imChatMessageTemplateType.getId(), imChatMessageTemplateValue);

					}

				}


		}

		*/

	}

}
