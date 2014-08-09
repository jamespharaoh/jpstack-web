package wbs.apn.chat.tv.core.console;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatTvConfiguration")
public
class ChatTvConfiguration {

/*
	@Inject
	ChatTvSchemeConsoleHelper objectHelper;

	@Inject
	GenericConsoleModule genericConsoleModule;

	@Inject
	Provider<FormFieldsBuilder> fieldsBuilder;

	@Inject
	Provider<IntFormField> intField;

	@SingletonComponent ("chatTvSchemeFields")
	public
	List<FormField> chatTvSchemeFields () {

		return fieldsBuilder.get ()
			.consoleHelper (objectHelper)
			.fields (

				intField.get ()
					.name ("toScreenTextCharge")
					.label ("To-screen text charge"),

				intField.get ()
					.name ("toScreenPhotoCharge")
					.label ("To-screen photo charge"))

			.build ();

	}

	@PrototypeComponent ("chatTvSChemeDetailsPartFactory")
	public
	Provider<PagePart> chatTvSchemeDetailsPartFactory () {

		return genericConsoleModule.makeDetailsPartFactory (
			chatTvSchemeLookup,
			"chat_manage",
			"/chat_tv_settings",
			chatTvSchemeFields ());

	}
*/

}
