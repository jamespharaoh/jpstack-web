package wbs.services.messagetemplate.model;

import static wbs.framework.utils.etc.Misc.isNull;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import org.hibernate.TransientObjectException;

import wbs.framework.database.Database;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.RandomLogic;
import wbs.platform.queue.logic.QueueLogic;

public
class MessageTemplateEntryValueHooks
	extends AbstractObjectHooks<MessageTemplateEntryValueRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	RandomLogic randomLogic;

	@Inject
	Provider<ObjectManager> objectManager;

	@Inject
	Provider<QueueLogic> queueLogic;

	// indirect dependencies

	@Inject
	Provider<MessageTemplateFieldTypeObjectHelper>
	messageTemplateFieldTypeHelperProvider;

	@Inject
	Provider<MessageTemplateFieldValueObjectHelper>
	messageTemplateFieldValueHelperProvider;

	// implementation

	@Override
	public
	Object getDynamic (
			@NonNull MessageTemplateEntryValueRec entryValue,
			@NonNull String name) {

		MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper =
			messageTemplateFieldTypeHelperProvider.get ();

		MessageTemplateEntryTypeRec entryType =
			entryValue.getMessageTemplateEntryType ();

		// find the ticket field type

		MessageTemplateFieldTypeRec fieldType =
			messageTemplateFieldTypeHelper.findByCode (
				entryType,
				name);

		try {

			// find the message template value

			MessageTemplateFieldValueRec fieldValue =
				entryValue.getFields ().get (
					fieldType.getId ());

			if (fieldValue == null) {

				return fieldType.getDefaultValue ();

			} else {

				return fieldValue.getStringValue ();

			}

		} catch (TransientObjectException exception) {

			// object not yet saved so fields will all be null

			return null;

		}

	}

	@Override
	public
	void setDynamic (
			@NonNull MessageTemplateEntryValueRec entryValue,
			@NonNull String name,
			Object value) {

		MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper =
			messageTemplateFieldTypeHelperProvider.get ();

		MessageTemplateFieldValueObjectHelper messageTemplateFieldValueHelper =
			messageTemplateFieldValueHelperProvider.get ();

		MessageTemplateEntryTypeRec entryType =
			entryValue.getMessageTemplateEntryType ();

		// find the ticket field type

		MessageTemplateFieldTypeRec fieldType =
			messageTemplateFieldTypeHelper.findByCode (
				entryType,
				name);

		/*
		List<String> messageTemplateUsedParameters =
			new ArrayList<String> ();

		String message =
			(String) value;

		// length of non variable parts

		Integer messageLength = 0;

		String[] parts =
			message.split (
				"\\{(.*?)\\}");

		for (int i = 0; i < parts.length; i++) {

			// length of special chars if gsm encoding

			if (
				equal (
					messageTemplateType.getCharset (),
					MessageTemplateTypeCharset.gsm)
			) {

				if (! Gsm.isGsm (parts [i])) {

					throw new RuntimeException (
						"Message text is invalid");

				}

				messageLength +=
					Gsm.length (parts [i]);

			} else {

				messageLength +=
					parts [i].length ();

			}

		}

		// length of the parameters

		Pattern regExp =
			Pattern.compile ("\\{(.*?)\\}");

		Matcher matcher =
			regExp.matcher (message);

		while (matcher.find ()) {

			String parameterName =
				matcher.group (1);

			MessageTemplateParameterRec messageTemplateParameter =
				messageTemplateParameterHelper.get ().findByCode (
					messageTemplateType,
					parameterName);

			if (messageTemplateParameter == null) {

				throw new RuntimeException (
					stringFormat (
						"The parameter %s does not exist!",
						parameterName));

			}

			if (messageTemplateParameter.getLength () != null) {

				messageLength +=
					messageTemplateParameter.getLength ();

			}

			messageTemplateUsedParameters.add (
				messageTemplateParameter.getName ());

		}

		// check if the rest of parameters which are not present were
		// required

		for (
			MessageTemplateParameterRec messageTemplateParameter
				: messageTemplateType.getMessageTemplateParameters ()
		) {

			if (
				doesNotContain (
					messageTemplateUsedParameters,
					messageTemplateParameter.getName ())
				&& messageTemplateParameter.getRequired ()
			) {

				throw new RuntimeException (
					stringFormat (
						"Parameter %s required but not present",
						messageTemplateParameter.getName ()));

			}

		}

		// check if the length is correct

		if (
			messageLength < messageTemplateType.getMinLength () ||
			messageLength > messageTemplateType.getMaxLength ())
		{

			throw new RuntimeException (
				"The message length is out of its template type bounds!");

		}
		*/

		// find or create value

		MessageTemplateFieldValueRec fieldValue =
			entryValue.getFields ().get (
				fieldType.getId ());

		if (
			isNull (
				fieldValue)
		) {

			fieldValue =
				messageTemplateFieldValueHelper.insert (
					messageTemplateFieldValueHelper.createInstance ()

				.setMessageTemplateEntryValue (
					entryValue)

				.setMessageTemplateFieldType (
					fieldType)

			);

		}

		fieldValue

			.setStringValue (
				(String)
				value);

	}

}