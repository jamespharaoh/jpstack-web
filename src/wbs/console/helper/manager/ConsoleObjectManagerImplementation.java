package wbs.console.helper.manager;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
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
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
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
	void setup () {

		consoleHelpersByObjectClass =
			mapWithDerivedKey (
				consoleHelpers,
				ConsoleHelper::objectClass);

		consoleHelpersByObjectName =
			mapWithDerivedKey (
				consoleHelpers,
				ConsoleHelper::objectName);

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"writeTdForObject");

		ConsoleHelper <?> objectHelper =
			findConsoleHelperRequired (
				object);

		String path =
			objectManager.objectPathMini (
				object,
				assumedRootOptional);

		if (

			link

			&& canView (
				taskLogger,
				object)

		) {

			new HtmlTableCellWriter ()

				.href (
					requestContext.resolveLocalUrl (
						objectHelper.getDefaultLocalPathGeneric (
							taskLogger,
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

	@Override
	public
	void writeHtmlForObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"writeHtmlForObject");

		if (taskLogger.debugEnabled ()) {

			taskLogger.debugFormat (
				"%s.htmlForObject (%s, %s, %s)",
				getClass ().getName (),
				objectManager.objectPath (
					object),
				optionalMapRequiredOrDefault (
					objectManager::objectPath,
					assumedRootOptional,
					"—"),
				Boolean.toString (
					mini));

		}

		ConsoleHelper <?> objectHelper =
			findConsoleHelperRequired (
				object);

		objectHelper.writeHtmlGeneric (
			taskLogger,
			formatWriter,
			object,
			assumedRootOptional,
			mini);

	}

	@Override
	public
	void objectToSimpleHtml (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"objectToSimpleHtml");

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
				taskLogger,
				formatWriter,
				dataObject,
				optionalFromNullable (
					assumedRoot),
				mini);

		} else if (
			isNull (
				object)
		) {

			taskLogger.warningFormat (
				"Null object is deprecated");

			formatWriter.writeFormat (
				"NULL");

		} else {

			throw new IllegalArgumentException ();

		}

	}

	@Override
	public
	boolean canView (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"canView (%s)",
				object.toString ());

		ConsoleHelper <?> objectHelper =
			findConsoleHelperRequired (
				object);

		return objectHelper.canView (
			taskLogger,
			genericCastUnchecked (
				object));

	}

	@Override
	public
	String contextName (
			@NonNull TaskLogger parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"contextLink");

		ConsoleHelper <?> objectHelper =
			findConsoleHelperRequired (
				object);

		return requestContext.resolveContextUrl (
			objectHelper.getDefaultContextPathGeneric (
				taskLogger,
				object));

	}

	@Override
	public
	String localLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"localLink");

		ConsoleHelper <?> objectHelper =
			findConsoleHelperRequired (
				object);

		return requestContext.resolveLocalUrl (
			objectHelper.getDefaultLocalPathGeneric (
				taskLogger,
				object));

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
			Record<?> object,
			Class <ObjectType> childClass) {

		return objectManager.getChildren (
			object,
			childClass);

	}

	@Override
	public
	Either <Optional <Record <?>>, String> getParentOrError (
			Record <?> object) {

		return objectManager.getParentOrError (
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
			Record <?> dataObject) {

		return objectManager.objectPath (
			dataObject);

	}

	@Override
	public
	String objectPath (
			Record <?> dataObject,
			Optional <Record <?>> root) {

		return objectManager.objectPath (
			dataObject,
			root);

	}

	@Override
	public
	String objectPath (
			@NonNull Record<?> dataObject,
			@NonNull Record<?> root) {

		return objectManager.objectPath (
			dataObject,
			root);

	}

	@Override
	public
	String objectPath (
			@NonNull Record<?> dataObject,
			@NonNull Optional<Record<?>> assumedRoot,
			boolean mini,
			boolean preload) {

		return objectManager.objectPath (
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
	public <ObjectType extends Record<?>>
	SortedMap<String,ObjectType> pathMap (
			Collection<ObjectType> objects,
			Record<?> assumedRoot,
			boolean mini) {

		return objectManager.pathMap (
			objects,
			assumedRoot,
			mini);

	}

	@Override
	public <ObjectType extends Record <?>>
	ObjectType update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectType object) {

		return objectManager.update (
			parentTaskLogger,
			object);

	}

	@Override
	public
	String getCode (
			Record<?> object) {

		return objectManager.getCode (
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
			Record<?> object) {

		return objectManager.getGlobalId (
			object);

	}

	@Override
	public
	GlobalId getParentGlobalId (
			Record<?> object) {

		return objectManager.getParentGlobalId (
			object);

	}

	@Override
	public
	Record<?> findObject (
			GlobalId objectGlobalId) {

		return objectManager.findObject (
			objectGlobalId);

	}

	@Override
	public
	List<Record<?>> getMinorChildren (
			Record<?> parent) {

		return objectManager.getMinorChildren (
			parent);

	}

	@Override
	public <ObjectType extends EphemeralRecord<?>>
	ObjectType remove (
			ObjectType object) {

		return objectManager
			.remove (object);

	}

	@Override
	public
	String getObjectTypeCode (
			Record<?> object) {

		return objectManager
			.getObjectTypeCode (object);

	}

	@Override
	public
	Long getObjectTypeId (
			Record<?> parentObject) {

		return objectManager.getObjectTypeId (
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
			Record<?> object,
			Record<?> parent) {

		return objectManager
			.isParent (
				object,
				parent);

	}

	@Override
	public <ObjectType extends Record<?>>
	ObjectType firstParent (
			Record<?> object,
			Set<ObjectType> parents) {

		return objectManager
			.firstParent (
				object,
				parents);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObjectNameRequired (
			String objectName) {

		return objectManager.objectHelperForObjectNameRequired (
			objectName);

	}

	@Override
	public
	Either <Optional <Object>, String> dereferenceOrError (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return objectManager.dereferenceOrError (
			object,
			path,
			hints);

	}

	@Override
	public
	Optional<Class<?>> dereferenceType (
			Optional<Class<?>> objectClass,
			Optional<String> path) {

		return objectManager.dereferenceType (
			objectClass,
			path);

	}

	@Override
	public
	String objectPathMini (
			Record<?> object) {

		return objectManager.objectPathMini (
			object);

	}

	@Override
	public
	String objectPathMini (
			Record<?> object,
			Optional<Record<?>> root) {

		return objectManager.objectPathMini (
			object,
			root);

	}

	@Override
	public
	String objectPathMini (
			Record<?> object,
			Record<?> root) {

		return objectManager.objectPathMini (
			object,
			root);

	}

	@Override
	public
	String objectPathMiniPreload (
			Record<?> object,
			Optional<Record<?>> root) {

		return objectManager.objectPathMiniPreload (
			object,
			root);

	}

	@Override
	public
	String objectPathMiniPreload (
			Record<?> object,
			Record<?> root) {

		return objectManager.objectPathMiniPreload (
			object,
			root);

	}

	@Override
	public
	<ObjectType extends Record<ObjectType>>
	Optional<ObjectType> getAncestor (
			Class<ObjectType> ancestorClass,
			Record<?> object) {

		return objectManager.getAncestor (
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
