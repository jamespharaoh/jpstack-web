package wbs.platform.object.settings;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsAction")
public
class ObjectSettingsAction <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ObjectLookup <ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	Provider <Responder> detailsResponder;

	@Getter @Setter
	Provider <Responder> accessDeniedResponder;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String objectRefName;

	@Getter @Setter
	String objectType;

	@Getter @Setter
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

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
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// check access

		if (! requestContext.canContext (editPrivKey)) {

			requestContext.addError (
				"Access denied");

			return accessDeniedResponder
				.get ();

		}

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ObjectSettingsAction.goReal ()",
					this);

		) {

			object =
				objectLookup.lookupObject (
					requestContext.consoleContextStuffRequired ());

			// perform update

			if (formFieldsProvider != null) {

				prepareParent ();

				prepareFieldSet (
					taskLogger);

			}

			UpdateResultSet updateResultSet =
				formFieldLogic.update (
					taskLogger,
					requestContext,
					formFieldSet,
					object,
					emptyMap (),
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
					taskLogger,
					formFieldSet,
					updateResultSet,
					object,
					(PermanentRecord <?>) object,
					optionalAbsent (),
					optionalAbsent (),
					"settings");

			} else {

				PermanentRecord <?> linkObject =
					genericCastUnchecked (
						objectManager.getParentRequired (
							object));

				Object objectRef =
					PropertyUtils.propertyGetAuto (
						object,
						objectRefName);

				formFieldLogic.runUpdateHooks (
					taskLogger,
					formFieldSet,
					updateResultSet,
					object,
					linkObject,
					optionalOf (
						objectRef),
					optionalOf (
						objectType),
					"settings");

			}

			// commit

			transaction.commit ();

			requestContext.addNotice (
				"Details updated");

			return detailsResponder.get ();

		}

	}

	void prepareParent () {

		ConsoleHelper <ParentType> parentHelper =
			objectManager.findConsoleHelperRequired (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.findRequired (
					0l);

			return;

		}

		Optional <Long> parentIdOptional =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (
			optionalIsPresent (
				parentIdOptional)
		) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					optionalGetRequired (
						parentIdOptional));

			return;

		}

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				parentTaskLogger,
				object);

	}

}
