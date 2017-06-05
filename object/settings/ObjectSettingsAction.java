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
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
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
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
	ConsoleFormType <ObjectType> formType;

	// state

	ObjectType object;
	ParentType parent;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return detailsResponder.get ();

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// check access

			if (! requestContext.canContext (editPrivKey)) {

				requestContext.addError (
					"Access denied");

				return accessDeniedResponder
					.get ();

			}

			// lookup objects

			object =
				objectLookup.lookupObject (
					transaction,
					requestContext.consoleContextStuffRequired ());

			// perform update

			/*
			if (formFieldsProvider != null) {

				prepareParent (
					transaction);

				prepareFieldSet (
					transaction);

			}
			*/

			ConsoleForm <ObjectType> form =
				formType.buildAction (
					transaction,
					emptyMap (),
					object);

			form.update (
				transaction);

			if (form.errors ()) {

				form.reportErrors (
					transaction);

				return null;

			}

			if (! form.updates ()) {

				requestContext.addWarning (
					"No changes made");

				return null;

			}

			// create events

			if (object instanceof PermanentRecord) {

				form.runUpdateHooks (
					transaction,
					(PermanentRecord <?>) object,
					optionalAbsent (),
					optionalAbsent ());

			} else {

				PermanentRecord <?> linkObject =
					genericCastUnchecked (
						objectManager.getParentRequired (
							transaction,
							object));

				Object objectRef =
					PropertyUtils.propertyGetAuto (
						object,
						objectRefName);

				form.runUpdateHooks (
					transaction,
					linkObject,
					optionalOf (
						objectRef),
					optionalOf (
						objectType));

			}

			// commit

			transaction.commit ();

			requestContext.addNotice (
				"Settings updated");

			return detailsResponder.get ();

		}

	}

	void prepareParent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareParent");

		) {

			ConsoleHelper <ParentType> parentHelper =
				objectManager.findConsoleHelperRequired (
					consoleHelper.parentClassRequired ());

			if (parentHelper.isRoot ()) {

				parent =
					parentHelper.findRequired (
						transaction,
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
						transaction,
						optionalGetRequired (
							parentIdOptional));

				return;

			}

		}

	}

	/*
	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				parentTaskLogger,
				object);

	}
	*/

}
