package wbs.platform.object.settings;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsAction")
public
class ObjectSettingsAction<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ObjectLookup<ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper<ObjectType> consoleHelper;

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

	@Getter @Setter
	FieldsProvider<ObjectType,ParentType> formFieldsProvider;

	// state

	ObjectType object;
	ParentType parent;

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
				"ObjectSettingsAction.goReal ()",
				this);

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		// perform update

		if (formFieldsProvider != null) {

			prepareParent ();

			prepareFieldSet ();

		}

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				requestContext,
				formFieldSet,
				object,
				ImmutableMap.of (),
				"settings");

		if (updateResultSet.errorCount () > 0) {

			formFieldLogic.reportErrors (
				requestContext,
				updateResultSet,
				"settings");

			requestContext.request (
				"objectSettingsUpdateResultSet",
				updateResultSet);

			return null;

		}

		if (updateResultSet.updateCount () == 0) {

			requestContext.addWarning (
				"No changes made");

			return null;

		}

		// create events

		if (object instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				(PermanentRecord<?>) object,
				Optional.<Object>absent (),
				Optional.<String>absent (),
				"settings");

		} else {

			PermanentRecord<?> linkObject =
				(PermanentRecord<?>)
				objectManager.getParent (object);

			Object objectRef =
				BeanLogic.getProperty (
					object,
					objectRefName);

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				linkObject,
				Optional.of (
					objectRef),
				Optional.of (
					objectType),
				"settings");

		}

		// commit

		transaction.commit ();

		requestContext.addNotice (
			"Details updated");

		return detailsResponder.get ();

	}

	void prepareParent () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ParentType> parentHelper =
			(ConsoleHelper<ParentType>)
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.findRequired (
					0l);

			return;

		}

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (parentId != null) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					parentId);

			return;

		}

	}

	void prepareFieldSet () {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				object);

	}

}
