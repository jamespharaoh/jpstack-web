package wbs.platform.object.create;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitSlash;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;

import java.util.Date;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
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

import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.etc.PropertyUtils;

import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	UpdateManager updateManager;

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
	FormContextBuilder <ObjectType> formContextBuilder;

	@Getter @Setter
	String createTimeFieldName;

	@Getter @Setter
	String createUserFieldName;

	// state

	ConsoleHelper <ParentType> parentHelper;
	ParentType parent;

	FormContext <ObjectType> formContext;

	ConsoleContext targetContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			responderName ());

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		// begin transaction

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			parentHelper =
				genericCastUnchecked (
					objectManager.findConsoleHelperRequired (
						consoleHelper.parentClass ()));

			// determine parent

			determineParent (
				transaction);

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

				Record <?> createDelegate =
					ifNotNullThenElse (
						createPrivDelegate,
						() -> (Record <?>)
							objectManager.dereferenceRequired (
								transaction,
								parent,
								createPrivDelegate),
						() -> parent);

				if (
					! privChecker.canRecursive (
						transaction,
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

				PropertyUtils.propertySetAuto (
					object,
					consoleHelper.typeCodeFieldName (),
					typeCode);

			}

			// perform updates

			/*
			if (formFieldsProvider != null) {

				prepareFieldSet (
					transaction);

			}*/

			formContext =
				formContextBuilder.build (
					transaction,
					emptyMap ());

			formContext.update (
				transaction);

			if (formContext.errors ()) {

				formContext.reportErrors (
					transaction);

				return null;

			}

			// set create time

			if (createTimeFieldName != null) {

				Class<?> createTimeFieldClass =
					PropertyUtils.propertyClassForObject (
						object,
						createTimeFieldName);

				if (createTimeFieldClass == Instant.class) {

					PropertyUtils.propertySetAuto (
						object,
						createTimeFieldName,
						transaction.now ());

				} else if (createTimeFieldClass == Date.class) {

					PropertyUtils.propertySetAuto (
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

				PropertyUtils.propertySetAuto (
					object,
					createUserFieldName,
					userConsoleLogic.userRequired (
						transaction));

			}

			// before create hook

			consoleHelper ().consoleHooks ().beforeCreate (
				transaction,
				object);

			// insert

			consoleHelper.insert (
				transaction,
				object);

			// after create hook

			consoleHelper ().consoleHooks ().afterCreate (
				transaction,
				object);

			// create event

			Object objectRef =
				consoleHelper.codeExists ()
					? consoleHelper.getCode (object)
					: object.getId ();

			if (consoleHelper.ephemeral ()) {

				eventLogic.createEvent (
					transaction,
					"object_created_in",
					userConsoleLogic.userRequired (
						transaction),
					objectRef,
					consoleHelper.shortName (),
					parent);

			} else {

				eventLogic.createEvent (
					transaction,
					"object_created",
					userConsoleLogic.userRequired (
						transaction),
					object,
					parent);

			}

			// update events

			if (object instanceof PermanentRecord) {

				formContext.runUpdateHooks (
					transaction,
					(PermanentRecord <?>) object,
					optionalAbsent (),
					optionalAbsent ());

			} else {

				formContext.runUpdateHooks (
					transaction,
					(PermanentRecord<?>) parent,
					optionalOf (
						objectRef),
					optionalOf (
						consoleHelper.shortName ()));

			}

			// signal update

			updateManager.signalUpdate (
				transaction,
				"user_privs",
				userConsoleLogic.userIdRequired ());

			updateManager.signalUpdate (
				transaction,
				"privs",
				0l);

			// commit transaction

			transaction.commit ();

			// prepare next page

			requestContext.addNotice (
				stringFormat (
					"%s created",
					capitalise (
						consoleHelper.shortName ())));

			requestContext.setEmptyFormData ();

			privChecker.refresh (
				transaction);

			List <String> targetContextTypeNameParts =
				stringSplitSlash (
					targetContextTypeName);

			if (
				collectionHasOneElement (
					targetContextTypeNameParts)
			) {

				ConsoleContextType targetContextType =
					consoleManager.contextType (
						targetContextTypeName,
						true);

				ConsoleContext targetContext =
					consoleManager.relatedContextRequired (
						transaction,
						requestContext.consoleContextRequired (),
						targetContextType);

				consoleManager.changeContext (
					transaction,
					targetContext,
					"/" + object.getId ());

			} else if (
				collectionHasTwoElements (
					targetContextTypeNameParts)
			) {

				ConsoleContextType targetParentContextType =
					consoleManager.contextType (
						listFirstElementRequired (
							targetContextTypeNameParts),
						true);

				ConsoleContext targetParentContext =
					consoleManager.relatedContextRequired (
						transaction,
						requestContext.consoleContextRequired (),
						targetParentContextType);

				ConsoleContextType targetContextType =
					consoleManager.contextType (
						listSecondElementRequired (
							targetContextTypeNameParts),
						true);

				ConsoleContext targetContext =
					consoleManager.relatedContextRequired (
						transaction,
						targetParentContext,
						targetContextType);

				consoleManager.changeContext (
					transaction,
					targetContext,
					"/" + object.getId ());

			}

			return responder (
				targetResponderName);

		}

	}

	void determineParent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"determineParent");

		) {

			if (parentHelper.isRoot ()) {

				ParentType parentTemp1 =
					genericCastUnchecked (
						rootHelper.findRequired (
							transaction,
							0l));

				parent =
					parentTemp1;

				return;

			}

			// get parent id from context

			Optional <Long> parentIdOptional =
				requestContext.stuffInteger (
					parentHelper.idKey ());

			// or from form

			if (
				optionalIsNotPresent (
					parentIdOptional)
			) {

				String parentIdString =
					optionalOrNull (
						requestContext.form (
							stringFormat (
								"create.%s",
								consoleHelper.parentFieldName ())));

				if (
					isNotNull (
						parentIdString)
				) {

					parentIdOptional =
						optionalOf (
							Long.parseLong (
								parentIdString));

				}

			}

			// error if not found

			if (
				optionalIsNotPresent (
					parentIdOptional)
			) {

				requestContext.addError (
					"Must set parent");

				return;

			}

			// retrieve from database

			parent =
				parentHelper.findRequired (
					transaction,
					optionalGetRequired (
						parentIdOptional));

		}

	}

	/*
	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parentTaskLogger,
					parent)
				: formFieldsProvider.getStaticFields ();

	}
	*/

}
