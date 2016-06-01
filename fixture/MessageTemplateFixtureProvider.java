package wbs.services.messagetemplate.fixture;

import javax.inject.Inject;

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
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateParameterObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeCharset;

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
	MessageTemplateEntryTypeObjectHelper messageTemplateEntryTypeHelper;

	@Inject
	MessageTemplateEntryValueObjectHelper messageTemplateEntryValueHelper;

	@Inject
	MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper;

	@Inject
	MessageTemplateFieldValueObjectHelper messageTemplateFieldValueHelper;

	@Inject
	MessageTemplateParameterObjectHelper messageTemplateParameterHelper;

	@Inject
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	RandomLogic randomLogic;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"message_template")

			.setName (
				"Message templates")

			.setDescription (
				"Message templates")

			.setLabel (
				"Message templates")

			.setTargetPath (
				"/messageTemplateDatabases")

			.setTargetFrame (
				"main")

		);

		MessageTemplateDatabaseRec messageTemplateDatabase =
			messageTemplateDatabaseHelper.insert (
				messageTemplateDatabaseHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeOrNull (
					GlobalId.root,
					"test"))

			.setCode (
				"message_template_database")

			.setName (
				"My message template database")

			.setDescription (
				"Message template database description")

		);

		MessageTemplateEntryTypeRec entryType1 =
			messageTemplateEntryTypeHelper.insert (
				messageTemplateEntryTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"entry_type_1")

			.setName (
				"Entry type 1")

			.setDescription (
				"")

		);

		MessageTemplateFieldTypeRec fieldType1a =
			messageTemplateFieldTypeHelper.insert (
				messageTemplateFieldTypeHelper.createInstance ()

			.setMessageTemplateEntryType (
				entryType1)

			.setCode (
				"field_type_a")

			.setName (
				"Field type A")

			.setDescription (
				"")

			.setDefaultValue (
				"Template 1 Default Value")

			.setHelpText (
				"<p>Help text 1</p>")

			.setMinLength (
				1l)

			.setMaxLength (
				50l)

			.setCharset (
				MessageTemplateTypeCharset.unicode)

		);

		messageTemplateParameterHelper.insert (
			messageTemplateParameterHelper.createInstance ()

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"")

			.setMessageTemplateEntryType (
				entryType1)

			.setRequired (
				false)

		);

		MessageTemplateEntryTypeRec entryType2 =
			messageTemplateEntryTypeHelper.insert (
				messageTemplateEntryTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				"entry_type_2")

			.setName (
				"Entry type 2")

			.setDescription (
				"")

		);

		messageTemplateFieldTypeHelper.insert (
			messageTemplateFieldTypeHelper.createInstance ()

			.setMessageTemplateEntryType (
				entryType2)

			.setCode (
				"field_type_a")

			.setName (
				"Field type A")

			.setDescription (
				"")

			.setDefaultValue (
				"My name is {name}")

			.setHelpText (
				"<p>Help text 2</p>")

			.setMinLength (
				5l)

			.setMaxLength (
				20l)

			.setCharset (
				MessageTemplateTypeCharset.unicode)

		);

		messageTemplateParameterHelper.insert (
			messageTemplateParameterHelper.createInstance ()

			.setMessageTemplateEntryType (
				entryType2)

			.setCode (
				"parameter_type_a")

			.setName (
				"Parameter type A")

			.setDescription (
				"")

			.setRequired (
				true)

			.setMaximumLength (
				4l)

		);

		messageTemplateParameterHelper.insert (
			messageTemplateParameterHelper.createInstance ()

			.setMessageTemplateEntryType (
				entryType2)

			.setCode (
				"parameter_type_b")

			.setName (
				"Parameter type B")

			.setDescription (
				"")

			.setRequired (
				false)

			.setMaximumLength (
				3l)

		);

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
				"")

		);

		MessageTemplateEntryValueRec entryValue1 =
			messageTemplateEntryValueHelper.insert (
				messageTemplateEntryValueHelper.createInstance ()

			.setMessageTemplateSet (
				messageTemplateSet)

			.setMessageTemplateEntryType (
				entryType1)

		);

		messageTemplateFieldValueHelper.insert (
			messageTemplateFieldValueHelper.createInstance ()

			.setMessageTemplateEntryValue (
				entryValue1)

			.setMessageTemplateFieldType (
				fieldType1a)

			.setStringValue (
				"hello world")

		);

	}

}
