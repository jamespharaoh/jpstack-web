package wbs.platform.object.settings;

import javax.inject.Inject;
import javax.inject.Provider;

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
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsAction")
public
class ObjectSettingsAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	Provider<Responder> detailsResponder;

	@Getter @Setter
	Provider<Responder> accessDeniedResponder;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String objectRefName;

	@Getter @Setter
	String objectType;

	@Getter @Setter
	FormFieldSet formFieldSet;

	// details

	@Override
	public
	Responder backupResponder () {
		return detailsResponder.get ();
	}

	// implementation

	@Override
	public
	Responder goReal () {

		// check access

		if (! requestContext.canContext (editPrivKey)) {

			requestContext.addError (
				"Access denied");

			return accessDeniedResponder
				.get ();

		}

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		Record<?> object =
			(Record<?>)
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		// perform update

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				formFieldSet,
				object);

		if (updateResultSet.errorCount () > 0) {

			formFieldLogic.reportErrors (
				updateResultSet);

			return null;

		}

		// create events

		if (object instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				updateResultSet,
				object,
				(PermanentRecord<?>) object,
				null,
				null);

		} else {

			PermanentRecord<?> linkObject =
				(PermanentRecord<?>)
				objectManager.getParent (object);

			Object objectRef =
				BeanLogic.getProperty (
					object,
					objectRefName);

			formFieldLogic.runUpdateHooks (
				updateResultSet,
				object,
				linkObject,
				objectRef,
				objectType);

		}

		// commit

		transaction.commit ();

		requestContext.addNotice (
			"Details updated");

		return detailsResponder.get ();

	}

}
