package wbs.platform.object.settings;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectRemoveAction")
public
class ObjectRemoveAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
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
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectRemoveAction.goReal ()",
				this);

		EphemeralRecord<?> ephemeralObject =
			(EphemeralRecord<?>)
			objectHelper.lookupObject (
				requestContext.contextStuff ());

		objectHelper.remove (
			ephemeralObject);

		Record<?> parentObject =
			objectHelper.getParent (
				ephemeralObject);

		eventLogic.createEvent (
			"object_removed_in",
			userConsoleLogic.userRequired (),
			objectHelper.getCode (ephemeralObject),
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
				requestContext.consoleContext (),
				targetContextType);

		consoleManager.changeContext (
			targetContext,
			"/" + parentObject.getId ());

		return listResponder
			.get ();

	}

}
