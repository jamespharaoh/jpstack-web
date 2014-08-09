package wbs.platform.object.settings;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

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
	UserObjectHelper userHelper;

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
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

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
			myUser,
			objectHelper.getCode (ephemeralObject),
			objectHelper.shortName (),
			parentObject);

		transaction.commit ();

		requestContext.addNotice (
			stringFormat (
				"%s deleted",
				capitalise (objectHelper.friendlyName ())));

System.out.println ("LOOKING FOR CONTEXT TYPE " + nextContextTypeName);
		ConsoleContextType targetContextType =
			consoleManager.contextType (
				nextContextTypeName,
				true);
System.out.println ("GOT CONTEXT TYPE " + targetContextType);

		ConsoleContext targetContext =
			consoleManager.relatedContext (
				requestContext.consoleContext (),
				targetContextType);
System.out.println ("GOT CONTEXT " + targetContext);

		consoleManager.changeContext (
			targetContext,
			"/" + parentObject.getId ());

		return listResponder
			.get ();

	}

}
