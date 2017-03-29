package wbs.apn.chat.scheme.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;

public
class ChatSchemeChargesHooks
	implements ObjectHooks<ChatSchemeChargesRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper <ChatSchemeChargesRec> chatSchemeChargesHelper,
			@NonNull ObjectHelper <?> chatSchemeHelper,
			@NonNull Record <?> parent) {

		if (! (parent instanceof ChatSchemeRec))
			return;

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSingletons");

		ChatSchemeRec chatScheme =
			(ChatSchemeRec)
			parent;

		ChatSchemeChargesRec chatSchemeCharges =
			chatSchemeChargesHelper.insert (
				taskLogger,
				chatSchemeChargesHelper.createInstance ()

			.setChatScheme (
				chatScheme)

		);

		chatScheme

			.setCharges (
				chatSchemeCharges);

	}

}