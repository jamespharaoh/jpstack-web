package wbs.clients.apn.chat.settings.console;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("chat-user-code-field")
@PrototypeComponent ("chatUserCodeFormFieldSpec")
@ConsoleModuleData
public
class ChatUserCodeFormFieldSpec {

	/*
	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	EventLogic eventLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;
	*/

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Integer size = FormField.defaultSize;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

	/*
	// implementation

	@Override
	protected
	ChatUserRec stringToType (
			String string)
		throws InvalidFormValueException {

		if (string.isEmpty ())
			return null;

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		ChatUserRec chatUser =
			chatUserHelper.findByCode (
				chat,
				string);

		if (chatUser == null) {

			requestContext.addError ("Chat user not found: " + string);

			throw new InvalidFormValueException ();

		}

		return chatUser;

	}

	@Override
	protected
	String typeToString (
			ChatUserRec value) {

		if (value == null)
			return null;

		return value.getCode ();

	}
	*/

}
