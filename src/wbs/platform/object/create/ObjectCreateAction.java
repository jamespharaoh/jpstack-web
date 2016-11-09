package wbs.platform.object.create;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreateAction")
public
class ObjectCreateAction <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

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
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	String createTimeFieldName;

	@Getter @Setter
	String createUserFieldName;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	// state

	ConsoleHelper <ParentType> parentHelper;
	ParentType parent;

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
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		parentHelper =
			genericCastUnchecked (
				objectManager.findConsoleHelperRequired (
					consoleHelper.parentClass ()));

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectCreateAction.goReal ()",
				this);

		// determine parent

		determineParent ();

		if (
			isNull (
				parent)
		) {
			return null;
		}

		// check permissions

		if (
			isNotNull (
				createPrivCode)
		) {

			Record<?> createDelegate =
				createPrivDelegate != null
					? (Record<?>) objectManager.dereference (
						parent,
						createPrivDelegate)
					: parent;

			if (
				! privChecker.canRecursive (
					createDelegate,
					createPrivCode)
			) {

				requestContext.addError (
					"Permission denied");

				return null;

			}

		}

		// create new record

		ObjectType object =
			consoleHelper.createInstance ();

		// set parent

		if (

			isNotNull (
				parent)

			&& ! parentHelper.isRoot ()

		) {

			consoleHelper.setParent (
				object,
				parent);

		}

		// set type code

		if (consoleHelper.typeCodeExists ()) {

			PropertyUtils.setProperty (
				object,
				consoleHelper.typeCodeFieldName (),
				typeCode);

		}

		// perform updates

		if (formFieldsProvider != null) {
			prepareFieldSet();
		}

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				requestContext,
				formFieldSet,
				object,
				ImmutableMap.of (),
				"create");

		if (updateResultSet.errorCount () > 0) {

			formFieldLogic.reportErrors (
				requestContext,
				updateResultSet,
				"create");

			requestContext.request (
				"objectCreateUpdateResultSet",
				updateResultSet);

			return null;

		}

		// set create time

		if (createTimeFieldName != null) {

			Class<?> createTimeFieldClass =
				PropertyUtils.propertyClassForObject (
					object,
					createTimeFieldName);

			if (createTimeFieldClass == Instant.class) {

				PropertyUtils.setProperty (
					object,
					createTimeFieldName,
					transaction.now ());

			} else if (createTimeFieldClass == Date.class) {

				PropertyUtils.setProperty (
					object,
					createTimeFieldName,
					instantToDateNullSafe (
						transaction.now ()));

			} else {

				throw new RuntimeException ();

			}

		}

		// set create user

		if (createUserFieldName != null) {

			PropertyUtils.setProperty (
				object,
				createUserFieldName,
				userConsoleLogic.userRequired ());

		}

		// before create hook

		consoleHelper ().consoleHooks ().beforeCreate (
			object);

		// insert

		consoleHelper.insert (
			object);

		// after create hook

		consoleHelper ().consoleHooks ().afterCreate (
			object);

		// create event

		Object objectRef =
			consoleHelper.codeExists ()
				? consoleHelper.getCode (object)
				: object.getId ();

		if (consoleHelper.ephemeral ()) {

			eventLogic.createEvent (
				"object_created_in",
				userConsoleLogic.userRequired (),
				objectRef,
				consoleHelper.shortName (),
				parent);

		} else {

			eventLogic.createEvent (
				"object_created",
				userConsoleLogic.userRequired (),
				object,
				parent);

		}

		// update events

		if (object instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				(PermanentRecord<?>) object,
				Optional.<Object>absent (),
				Optional.<String>absent (),
				"create");

		} else {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				(PermanentRecord<?>) parent,
				Optional.of (
					objectRef),
				Optional.of (
					consoleHelper.shortName ()),
				"create");

		}

		// commit transaction

		transaction.commit ();

		// prepare next page

		requestContext.addNotice (
			stringFormat (
				"%s created",
				capitalise (
					consoleHelper.shortName ())));

		requestContext.setEmptyFormData ();

		privChecker.refresh ();

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				targetContextTypeName,
				true);

		ConsoleContext targetContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				targetContextType);

		consoleManager.changeContext (
			targetContext,
			"/" + object.getId ());

		return responder (
			targetResponderName);

	}

	void determineParent () {

		if (parentHelper.isRoot ()) {

			@SuppressWarnings ("unchecked")
			ParentType parentTemp1 =
				(ParentType)
				rootHelper.findRequired (
					0l);

			parent =
				parentTemp1;

			return;

		}

		// get parent id from context

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		// or from form

		if (
			isNull (
				parentId)
		) {

			String parentIdString =
				optionalOrNull (
					requestContext.form (
						stringFormat (
							"create-%s",
							consoleHelper.parentFieldName ())));

			if (
				isNotNull (
					parentIdString)
			) {

				parentId =
					Long.parseLong (
						parentIdString);

			}

		}

		// error if not found

		if (
			isNull (
				parentId)
		) {

			requestContext.addError (
				"Must set parent");

			return;

		}

		// retrieve from database

		parent =
			parentHelper.findRequired (
				parentId);

	}

	void prepareFieldSet () {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parent)
				: formFieldsProvider.getStaticFields ();

	}

}
