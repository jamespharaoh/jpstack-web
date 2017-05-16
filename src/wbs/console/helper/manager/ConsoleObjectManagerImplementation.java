package wbs.console.helper.manager;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.html.HtmlTableCellWriter;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

/**
 * Performs console-relevant operations on DataObjects such as resolving names
 * and checking privs. At the back end this delegates to ObjectHelper objects
 * which are provided by the various ConsoleModules.
 */
@SingletonComponent ("consoleObjectManager")
public
class ConsoleObjectManagerImplementation
	implements ConsoleObjectManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// collection dependencies

	@SingletonDependency
	List <ConsoleHelper <?>> consoleHelpers;

	// state

	Map <Class <?>, ConsoleHelper <?>> consoleHelpersByObjectClass;
	Map <String, ConsoleHelper <?>> consoleHelpersByObjectName;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			consoleHelpersByObjectClass =
				mapWithDerivedKey (
					consoleHelpers,
					ConsoleHelper::objectClass);

			consoleHelpersByObjectName =
				mapWithDerivedKey (
					consoleHelpers,
					ConsoleHelper::objectName);

		}

	}

	// implementation

	@Override
	public <RecordType extends Record <RecordType>>
	Optional <ConsoleHelper <RecordType>> findConsoleHelper (
			@NonNull Record <?> dataObject) {

		return findConsoleHelper (
			dataObject.getClass ());

	}

	@Override
	public <RecordType extends Record <RecordType>>
	Optional <ConsoleHelper <RecordType>> findConsoleHelper (
			@NonNull Class <?> objectClass) {

		Class <?> tempClass =
			objectClass;

		while (
			isSubclassOf (
				Record.class,
				tempClass)
		) {

			ConsoleHelper <?> consoleHelper =
				consoleHelpersByObjectClass.get (
					tempClass);

			if (
				isNotNull (
					consoleHelper)
			) {

				return optionalOf (
					genericCastUnchecked (
						consoleHelper));

			}

			tempClass =
				tempClass.getSuperclass ();

		}

		return optionalAbsent ();

	}

	@Override
	public
	Optional <ConsoleHelper <?>> findConsoleHelper (
			@NonNull String objectTypeName) {

		return optionalFromNullable (
			consoleHelpersByObjectName.get (
				objectTypeName));

	}

	@Override
	public
	void writeTdForObject (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writeTdForObject");

		) {

			ConsoleHelper <?> objectHelper =
				findConsoleHelperRequired (
					object);

			String path =
				objectManager.objectPathMini (
					transaction,
					object,
					assumedRootOptional);

			if (

				link

				&& canView (
					transaction,
					object)

			) {

				new HtmlTableCellWriter ()

					.href (
						requestContext.resolveLocalUrl (
							objectHelper.getDefaultLocalPathGeneric (
								transaction,
								object)))

					.target (
						"main")

					.columnSpan (
						colspan)

					.write (
						formatWriter);

				formatWriter.writeFormat (
					"%h</td>",
					path);

			} else {

				formatWriter.writeLineFormat (
					"<td>%h</td>",
					path);

			}

		}

	}

	@Override
	public
	void writeHtmlForObject (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writeHtmlForObject");

		) {

			if (transaction.debugEnabled ()) {

				transaction.debugFormat (
					"%s.htmlForObject (%s, %s, %s)",
					getClass ().getName (),
					objectManager.objectPath (
						transaction,
						object),
					optionalMapRequiredOrDefault (
						assumedRoot ->
							objectManager.objectPath (
								transaction,
								assumedRoot),
						assumedRootOptional,
						"—"),
					Boolean.toString (
						mini));

			}

			ConsoleHelper <?> objectHelper =
				findConsoleHelperRequired (
					object);

			objectHelper.writeHtmlGeneric (
				transaction,
				formatWriter,
				object,
				assumedRootOptional,
				mini);

		}

	}

	@Override
	public
	void objectToSimpleHtml (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"objectToSimpleHtml");

		) {

			if (
				object instanceof Integer
				|| object instanceof Long
			) {

				formatWriter.writeFormat (
					"%s",
					object.toString ());

			} else if (object instanceof Record) {

				Record <?> dataObject =
					(Record <?>) object;

				writeHtmlForObject (
					transaction,
					formatWriter,
					dataObject,
					optionalFromNullable (
						assumedRoot),
					mini);

			} else if (
				isNull (
					object)
			) {

				transaction.warningFormat (
					"Null object is deprecated");

				formatWriter.writeFormat (
					"NULL");

			} else {

				throw new IllegalArgumentException ();

			}

		}

	}

	@Override
	public
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			ConsoleHelper <?> objectHelper =
				findConsoleHelperRequired (
					object);

			return objectHelper.canView (
				transaction,
				genericCastUnchecked (
					object));

		}

	}

	@Override
	public
	String contextName (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		ConsoleHelper<?> objectHelper =
			findConsoleHelperRequired (
				object);

		if (objectHelper.typeCodeExists ()) {

			return stringFormat (
				"%s_%s",
				objectHelper.objectName (),
				objectHelper.getTypeCode (
					genericCastUnchecked (
						object)));

		} else {

			return objectHelper.objectName ();

		}

	}

	@Override
	public
	String contextLink (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"contextLink");

		) {

			ConsoleHelper <?> objectHelper =
				findConsoleHelperRequired (
					object);

			return requestContext.resolveContextUrl (
				objectHelper.getDefaultContextPathGeneric (
					transaction,
					object));

		}

	}

	@Override
	public
	String localLink (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localLink");

		) {

			ConsoleHelper <?> objectHelper =
				findConsoleHelperRequired (
					object);

			return requestContext.resolveLocalUrl (
				objectHelper.getDefaultLocalPathGeneric (
					transaction,
					object));

		}

	}

	// delegate to objectManager

	@Override
	public
	ObjectHelper <?> objectHelperForTypeCodeRequired (
			String typeCode) {

		return objectManager.objectHelperForTypeCodeRequired (
			typeCode);

	}

	@Override
	public
	ObjectHelper <?> objectHelperForClassRequired (
			Class <?> objectClass) {

		return objectManager.objectHelperForClassRequired (
			objectClass);

	}

	@Override
	public <ObjectType extends Record <ObjectType>>
	List <ObjectType> getChildren (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Class <ObjectType> childClass) {

		return objectManager.getChildren (
			parentTransaction,
			object,
			childClass);

	}

	@Override
	public
	Either <Optional <Record <?>>, String> getParentOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.getParentOrError (
			parentTransaction,
			object);

	}

	@Override
	public
	Class <?> objectClassForTypeCodeRequired (
			String typeCode) {

		return objectManager.objectClassForTypeCodeRequired (
			typeCode);

	}

	@Override
	public
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject) {

		return objectManager.objectPath (
			parentTransaction,
			dataObject);

	}

	@Override
	public
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject,
			@NonNull Optional <Record <?>> root) {

		return objectManager.objectPath (
			parentTransaction,
			dataObject,
			root);

	}

	@Override
	public
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject,
			@NonNull Record <?> root) {

		return objectManager.objectPath (
			parentTransaction,
			dataObject,
			root);

	}

	@Override
	public
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record<?> dataObject,
			@NonNull Optional<Record<?>> assumedRoot,
			boolean mini,
			boolean preload) {

		return objectManager.objectPath (
			parentTransaction,
			dataObject,
			assumedRoot,
			mini,
			preload);

	}

	@Override
	public
	String objectIdString (
			Record<?> dataObject) {

		return objectManager.objectIdString (
			dataObject);

	}

	@Override
	public <ObjectType extends Record <?>>
	SortedMap <String, ObjectType> pathMap (
			@NonNull Transaction parentTransaction,
			Collection <ObjectType> objects,
			Record <?> assumedRoot,
			boolean mini) {

		return objectManager.pathMap (
			parentTransaction,
			objects,
			assumedRoot,
			mini);

	}

	@Override
	public <ObjectType extends Record <?>>
	ObjectType update (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectType object) {

		return objectManager.update (
			parentTransaction,
			object);

	}

	@Override
	public
	String getCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.getCode (
			parentTransaction,
			object);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObjectRequired (
			Record<?> object) {

		return objectManager.objectHelperForObjectRequired (
			object);

	}

	@Override
	public
	GlobalId getGlobalId (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.getGlobalId (
			parentTransaction,
			object);

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.getParentGlobalId (
			parentTransaction,
			object);

	}

	@Override
	public
	Record <?> findObject (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId objectGlobalId) {

		return objectManager.findObject (
			parentTransaction,
			objectGlobalId);

	}

	@Override
	public
	List <Record <?>> getMinorChildren (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent) {

		return objectManager.getMinorChildren (
			parentTransaction,
			parent);

	}

	@Override
	public <ObjectType extends EphemeralRecord<?>>
	ObjectType remove (
			@NonNull Transaction parentTransaction,
			ObjectType object) {

		return objectManager.remove (
			parentTransaction,
			object);

	}

	@Override
	public
	String getObjectTypeCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.getObjectTypeCode (
			parentTransaction,
			object);

	}

	@Override
	public
	Long getObjectTypeId (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parentObject) {

		return objectManager.getObjectTypeId (
			parentTransaction,
			parentObject);

	}

	@Override
	public
	Long objectClassToTypeId (
			@NonNull Class<?> objectClass) {

		return objectManager.objectClassToTypeId (
			objectClass);

	}

	@Override
	public
	List<ObjectHelper<?>> objectHelpers () {

		return objectManager
			.objectHelpers ();

	}

	@Override
	public
	Optional <ObjectHelper <?>> objectHelperForTypeId (
			@NonNull Long typeId) {

		return objectManager.objectHelperForTypeId (
			typeId);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForTypeIdRequired (
			@NonNull Long typeId) {

		return objectManager.objectHelperForTypeIdRequired (
			typeId);

	}

	@Override
	@Deprecated
	public
	ObjectHelper <?> objectHelperForTypeIdOrNull (
			@NonNull Long typeId) {

		return objectManager.objectHelperForTypeIdOrNull (
			typeId);

	}

	@Override
	public
	boolean isParent (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> parent) {

		return objectManager.isParent (
			parentTransaction,
			object,
			parent);

	}

	@Override
	public <ObjectType extends Record <?>>
	ObjectType firstParent (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Set <ObjectType> parents) {

		return objectManager.firstParent (
			parentTransaction,
			object,
			parents);

	}

	@Override
	public
	ObjectHelper <?> objectHelperForObjectNameRequired (
			String objectName) {

		return objectManager.objectHelperForObjectNameRequired (
			objectName);

	}

	@Override
	public
	Either <Optional <Object>, String> dereferenceOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return objectManager.dereferenceOrError (
			parentTransaction,
			object,
			path,
			hints);

	}

	@Override
	public
	Optional <Class <?>> dereferenceType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <Class <?>> objectClass,
			@NonNull Optional <String> path) {

		return objectManager.dereferenceType (
			parentTaskLogger,
			objectClass,
			path);

	}

	@Override
	public
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return objectManager.objectPathMini (
			parentTransaction,
			object);

	}

	@Override
	public
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> root) {

		return objectManager.objectPathMini (
			parentTransaction,
			object,
			root);

	}

	@Override
	public
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> root) {

		return objectManager.objectPathMini (
			parentTransaction,
			object,
			root);

	}

	@Override
	public
	String objectPathMiniPreload (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> root) {

		return objectManager.objectPathMiniPreload (
			parentTransaction,
			object,
			root);

	}

	@Override
	public
	String objectPathMiniPreload (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> root) {

		return objectManager.objectPathMiniPreload (
			parentTransaction,
			object,
			root);

	}

	@Override
	public <ObjectType extends Record <ObjectType>>
	Optional <ObjectType> getAncestor (
			@NonNull Transaction parentTransaction,
			@NonNull Class <ObjectType> ancestorClass,
			@NonNull Record <?> object) {

		return objectManager.getAncestor (
			parentTransaction,
			ancestorClass,
			object);

	}

	@Override
	public
	Optional <Class <?>> objectClassForTypeCode (
			String typeCode) {

		return objectManager.objectClassForTypeCode (
			typeCode);

	}

}
