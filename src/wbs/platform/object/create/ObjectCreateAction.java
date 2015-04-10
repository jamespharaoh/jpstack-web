package wbs.platform.object.create;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreateAction")
public
class ObjectCreateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	PrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String typeCode;

	@Getter @Setter
	String responderName;

	@Getter @Setter
	String targetContextTypeName;

	@Getter @Setter
	String targetResponderName;

	@Getter @Setter
	String createPrivDelegate;

	@Getter @Setter
	String createPrivCode;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String createTimeFieldName;

	@Getter @Setter
	String createUserFieldName;

	// state

	ConsoleHelper<?> parentHelper;
	Record<?> parent;

	ConsoleContext targetContext;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			responderName ());

	}

	@Override
	protected
	Responder goReal () {

		parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// determine parent

		determineParent ();

		if (parent == null)
			return null;

		// check permissions

		if (createPrivCode != null) {

			Record<?> createDelegate =
				createPrivDelegate != null
					? (Record<?>) objectManager.dereference (
						parent,
						createPrivDelegate)
					: parent;

			if (! privChecker.can (
					createDelegate,
					createPrivCode)) {

				requestContext.addError (
					"Permission denied");

				return null;

			}

		}

		// create new record

		Record<?> object =
			consoleHelper.createInstance ();

		// set parent

		if (parent != null
				&& ! parentHelper.root ()) {

			consoleHelper.setParent (
				object,
				parent);

		}

		// set type code

		if (consoleHelper.typeCodeExists ()) {

			BeanLogic.setProperty (
				object,
				consoleHelper.typeCodeFieldName (),
				typeCode);

		}

		// perform updates

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				formFieldSet,
				object);

		if (updateResultSet.errorCount () > 0) {

			formFieldLogic.reportErrors (
				updateResultSet);

			return null;

		}

		// set create time

		if (createTimeFieldName != null) {

			BeanLogic.setProperty (
				object,
				createTimeFieldName,
				transaction.now ());

		}

		// set create user

		if (createUserFieldName != null) {

			BeanLogic.setProperty (
				object,
				createUserFieldName,
				myUser);

		}

		// insert

		objectManager.insert (
			object);

		// create event

		Object objectRef =
			consoleHelper.codeExists ()
				? consoleHelper.getCode (object)
				: object.getId ();

		if (consoleHelper.ephemeral ()) {

			eventLogic.createEvent (
				"object_created_in",
				myUser,
				objectRef,
				consoleHelper.shortName (),
				parent);

		} else {

			eventLogic.createEvent (
				"object_created",
				myUser,
				object,
				parent);

		}

		// update events

		if (object instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				updateResultSet,
				object,
				(PermanentRecord<?>) object,
				null,
				null);

		} else {

			formFieldLogic.runUpdateHooks (
				updateResultSet,
				object,
				(PermanentRecord<?>) parent,
				objectRef,
				consoleHelper.shortName ());

		}

		// commit transaction

		transaction.commit ();

		// prepare next page

		requestContext.addNotice (
			stringFormat (
				"%s created",
				capitalise (consoleHelper.shortName ())));

		requestContext.setEmptyFormData ();

		privChecker.refresh ();

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				targetContextTypeName,
				true);

		ConsoleContext targetContext =
			consoleManager.relatedContext (
				requestContext.consoleContext (),
				targetContextType);

		consoleManager.changeContext (
			targetContext,
			"/" + object.getId ());

		return responder (
			targetResponderName);

	}

	void determineParent () {

		if (parentHelper.root ()) {

			parent =
				rootHelper.find (0);

			return;

		}

		// get parent id from context

		Integer parentId =
			requestContext.stuffInt (
				parentHelper.idKey ());

		// or from form

		if (parentId == null) {

			parentId =
				toInteger (
					requestContext.getForm (
						consoleHelper.parentFieldName ()));

		}

		// error if not found

		if (parentId == null) {

			requestContext.addError (
				"Must set parent");

			return;

		}

		// retrieve from database

		parent =
			parentHelper.find (
				parentId);

		if (parent == null) {

			throw new RuntimeException (
				"Parent object not found");

		}

	}

}
