package wbs.services.messagetemplate.logic;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;
import lombok.NonNull;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.services.messagetemplate.fixture.MessageTemplateDatabaseSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateEntryTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateFieldTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateParameterSpec;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateParameterObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateTypeCharset;

@SingletonComponent ("messageTemplateLogic")
public
class MessageTemplateLogicImplementation
	implements MessageTemplateLogic {

	// dependencies

	@SingletonDependency
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;

	@SingletonDependency
	MessageTemplateEntryTypeObjectHelper messageTemplateEntryTypeHelper;

	@SingletonDependency
	MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper;

	@SingletonDependency
	MessageTemplateParameterObjectHelper messageTemplateParameterHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			@NonNull SliceRec slice,
			@NonNull String resourceName) {

		// load data

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				MessageTemplateDatabaseSpec.class,
				MessageTemplateEntryTypeSpec.class,
				MessageTemplateFieldTypeSpec.class,
				MessageTemplateParameterSpec.class);

		MessageTemplateDatabaseSpec databaseSpec =
			(MessageTemplateDatabaseSpec)
			dataFromXml.readClasspath (
				ImmutableList.<Object>of (),
				resourceName);

		// create database

		MessageTemplateDatabaseRec mtDatabase =
			messageTemplateDatabaseHelper.insert (
				messageTemplateDatabaseHelper.createInstance ()

			.setSlice (
				slice)

			.setCode (
				simplifyToCodeRequired (
					databaseSpec.name ()))

			.setName (
				databaseSpec.name ())

			.setDescription (
				emptyStringIfNull (
					databaseSpec.description ()))

		);

		// create entry types

		for (
			MessageTemplateEntryTypeSpec entryTypeSpec
				: databaseSpec.entryTypes ()
		) {

			MessageTemplateEntryTypeRec entryType =
				messageTemplateEntryTypeHelper.insert (
					messageTemplateEntryTypeHelper.createInstance ()

				.setMessageTemplateDatabase (
					mtDatabase)

				.setCode (
					simplifyToCodeRequired (
						entryTypeSpec.name ()))

				.setName (
					entryTypeSpec.name ())

				.setDescription (
					emptyStringIfNull (
						entryTypeSpec.description ()))

			);

			// create field types

			for (
				MessageTemplateFieldTypeSpec fieldTypeSpec
					: entryTypeSpec.fieldTypes ()
			) {

				messageTemplateFieldTypeHelper.insert (
					messageTemplateFieldTypeHelper.createInstance ()

					.setMessageTemplateEntryType (
						entryType)

					.setCode (
						simplifyToCodeRequired (
							fieldTypeSpec.name ()))

					.setName (
						fieldTypeSpec.name ())

					.setDescription (
						emptyStringIfNull (
							fieldTypeSpec.description ()))

					.setDefaultValue (
						fieldTypeSpec.value ())

					.setHelpText (
						"")

					.setCharset (
						MessageTemplateTypeCharset.unicode)

				);

			}

			// create parameter types

			for (
				MessageTemplateParameterSpec parameterSpec
					: entryTypeSpec.parameters ()
			) {

				messageTemplateParameterHelper.insert (
					messageTemplateParameterHelper.createInstance ()

					.setMessageTemplateEntryType (
						entryType)

					.setCode (
						simplifyToCodeRequired (
							parameterSpec.name ()))

					.setName (
						parameterSpec.name ())

					.setDescription (
						emptyStringIfNull (
							parameterSpec.description ()))

					.setMaximumLength (
						parameterSpec.maximumLength ())

				);

			}

		}

		return mtDatabase;

	}

}
