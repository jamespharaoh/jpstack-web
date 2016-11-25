package wbs.platform.object.settings;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectRemoveAction")
public
class ObjectRemoveAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> objectHelper;

	@Getter @Setter
	ConsoleHelper<?> parentHelper;

	@Getter @Setter
	Provider<Responder> settingsResponder;

	@Getter @Setter
	Provider<Responder> listResponder;

	@Getter @Setter
	String nextContextTypeName;

	@Getter @Setter
	String editPrivKey;

	// details

	@Override
	public
	Responder backupResponder () {
		return settingsResponder.get ();
	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectRemoveAction.goReal ()",
				this);

		Record <?> ephemeralObject =
			objectHelper.lookupObject (
				requestContext.contextStuff ());

		objectHelper.remove (
			ephemeralObject);

		Record <?> parentObject =
			objectHelper.getParentRequired (
				genericCastUnchecked (
					ephemeralObject));

		eventLogic.createEvent (
			"object_removed_in",
			userConsoleLogic.userRequired (),
			objectHelper.getCode (
				genericCastUnchecked (
					ephemeralObject)),
			objectHelper.shortName (),
			parentObject);

		transaction.commit ();

		requestContext.addNotice (
			stringFormat (
				"%s deleted",
				capitalise (
					objectHelper.friendlyName ())));

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				nextContextTypeName,
				true);

		ConsoleContext targetContext =
			consoleManager.relatedContextRequired (
				taskLogger,
				requestContext.consoleContext (),
				targetContextType);

		consoleManager.changeContext (
			taskLogger,
			targetContext,
			"/" + parentObject.getId ());

		return listResponder
			.get ();

	}

}
