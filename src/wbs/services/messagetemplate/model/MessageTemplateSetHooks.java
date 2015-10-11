package wbs.services.messagetemplate.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.TransientObjectException;

import wbs.framework.database.Database;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.RandomLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.sms.gsm.Gsm;

public
class MessageTemplateSetHooks
	extends AbstractObjectHooks<MessageTemplateSetRec> {

	@Inject
	Database database;

	@Inject
	RandomLogic randomLogic;

	@Inject
	Provider<ObjectManager> objectManager;

	@Inject
	Provider<QueueLogic> queueLogic;

	@Inject
	Provider<MessageTemplateSetObjectHelper> messageTemplateSetHelper;

	@Inject
	Provider<MessageTemplateTypeObjectHelper> messageTemplateTypeHelper;

	@Inject
	Provider<MessageTemplateValueObjectHelper> messageTemplateValueHelper;

	@Inject
	Provider<MessageTemplateParameterObjectHelper> messageTemplateParameterHelper;

	@Override
	public
	Object getDynamic (
			MessageTemplateSetRec messageTemplateSet,
			String name) {

		// find the ticket field type

		MessageTemplateTypeRec messageTemplateType =
			messageTemplateTypeHelper.get ().findByCode (
				messageTemplateSet.getMessageTemplateDatabase (),
				name);

		try {

			// find the message template value

			MessageTemplateValueRec messageTemplateValue =
				messageTemplateSet.getMessageTemplateValues ().get (
					messageTemplateType.getId ());

			if (messageTemplateValue == null) {

				return messageTemplateType.getDefaultValue ();

			} else {

				return messageTemplateValue.getStringValue ();

			}

		} catch (TransientObjectException exception) {

			// object not yet saved so fields will all be null

			return null;

		}

	}

	@Override
	public
	void setDynamic (
			MessageTemplateSetRec messageTemplateSet,
			String name,
			Object value) {

		// find the ticket field type

		MessageTemplateTypeRec messageTemplateType =
			messageTemplateTypeHelper.get ().findByCode (
				messageTemplateSet.getMessageTemplateDatabase (),
				name);

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
			throw new RuntimeException ("The message length is out of it's template type bounds!");
		}

		// if the length is correct and all the required parameters are present, the value is created

		MessageTemplateValueRec messageTemplateValue;

		try {

			messageTemplateValue =
				messageTemplateSet.getMessageTemplateValues().get(
					messageTemplateType.getId());
		}
		catch (Exception e) {
			messageTemplateValue =
				null;
		}

		// if the value object does not exist, a new one is created

		if (messageTemplateValue == null) {
			messageTemplateValue = new MessageTemplateValueRec()
				.setMessageTemplateSet(messageTemplateSet)
				.setMessageTemplateType(messageTemplateType);
		}


		messageTemplateValue.setStringValue((String)message);

		messageTemplateSet.setNumTemplates (
			messageTemplateSet.getNumTemplates() + 1);

		 messageTemplateSet.getMessageTemplateValues ().put (
			messageTemplateType.getId(),
			messageTemplateValue);

	}

}