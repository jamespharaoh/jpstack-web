package wbs.console.helper;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlTableCellWriter;

/**
 * Performs console-relevant operations on DataObjects such as resolving names
 * and checking privs. At the back end this delegates to ObjectHelper objects
 * which are provided by the various ConsoleModules.
 */
@Log4j
@SingletonComponent ("consoleObjectManager")
public
class ConsoleObjectManagerImplementation
	implements ConsoleObjectManager {

	// singleton dependencies

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// collection dependencies

	// (force instantiation)
	@SingletonDependency
	List <ConsoleHelper <?>> consoleHelpers;

	// implementation

	@Override
	public
	ConsoleHelper <?> findConsoleHelper (
			@NonNull Record <?> dataObject) {

		ConsoleHelper <?> objectHelper =
			findConsoleHelper (
				dataObject.getClass ());

		if (objectHelper == null) {

			throw new IllegalArgumentException (
				stringFormat (
					"No console object helper for %s",
					dataObject.getClass ().getName ()));

		}

		return objectHelper;

	}

	@Override
	public
	ConsoleHelper<?> findConsoleHelper (
			@NonNull Class<?> objectClass) {

		Class<?> tempClass =
			objectClass;

		while (Record.class.isAssignableFrom (tempClass)) {

			ConsoleHelper<?> consoleHelper =
				consoleHelperRegistry.findByObjectClass (
					tempClass);

			if (
				isNotNull (
					consoleHelper)
			) {

				return consoleHelper;

			}

			tempClass =
				tempClass.getSuperclass ();

		}

		return null;

	}

	@Override
	public
	ConsoleHelper<?> findConsoleHelper (
			@NonNull String objectTypeName) {

		return consoleHelperRegistry.findByObjectName (
			objectTypeName);

	}

	@Override
	public
	void writeTdForObject (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		ConsoleHelper <?> objectHelper =
			findConsoleHelper (
				object);

		String path =
			objectManager.objectPathMini (
				object,
				assumedRootOptional);

		if (

			link

			&& canView (
				object)

		) {

			new HtmlTableCellWriter ()

				.href (
					requestContext.resolveLocalUrl (
						objectHelper.getDefaultLocalPath (
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
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"%s.htmlForObject (%s, %s, %s)",
					getClass ().getName (),
					objectManager.objectPath (
						object),
					optionalMapRequiredOrDefault (
						objectManager::objectPath,
						assumedRootOptional,
						"—"),
					Boolean.toString (
						mini)));

		}

		ConsoleHelper <?> objectHelper =
			findConsoleHelper (
				object);

		objectHelper.writeHtml (
			formatWriter,
			object,
			assumedRootOptional,
			mini);

	}

	@Override
	public
	void objectToSimpleHtml (
			@NonNull FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini) {

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
				formatWriter,
				dataObject,
				optionalFromNullable (
					assumedRoot),
				mini);

		} else if (
			isNull (
				object)
		) {

			log.warn (
				new RuntimeException (
					"Null object is deprecated"));

			formatWriter.writeFormat (
				"NULL");

		} else {

			throw new IllegalArgumentException ();

		}

	}

	@Override
	public
	boolean canView (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			findConsoleHelper (
				object);

		return objectHelper.canView (
			object);

	}

	@Override
	public
	String contextName (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			findConsoleHelper (object);

		if (objectHelper.typeCodeExists ()) {

			return stringFormat (
				"%s_%s",
				objectHelper.objectName (),
				objectHelper.getTypeCode (object));

		} else {

			return objectHelper.objectName ();

		}

	}

	@Override
	public
	String contextLink (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			findConsoleHelper (object);

		return requestContext.resolveContextUrl (
			objectHelper.getDefaultContextPath (object));

	}

	@Override
	public
	String localLink (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			findConsoleHelper (object);

		return requestContext.resolveLocalUrl (
			objectHelper.getDefaultLocalPath (object));

	}

	// delegate to objectManager

	@Override
	public
	ObjectHelper<?> objectHelperForTypeCodeRequired (
			String typeCode) {

		return objectManager
			.objectHelperForTypeCodeRequired (typeCode);

	}

	@Override
	public
	ObjectHelper <?> objectHelperForClassRequired (
			Class <?> objectClass) {

		return objectManager.objectHelperForClassRequired (
			objectClass);

	}

	@Override
	public <ObjectType extends Record<ObjectType>>
	List<ObjectType> getChildren (
			Record<?> object,
			Class<ObjectType> childClass) {

		return objectManager
			.getChildren (
				object,
				childClass);

	}

	@Override
	public
	Record<?> getParent (
			Record<?> object) {

		return objectManager
			.getParent (object);

	}

	@Override
	public
	Class<?> objectClassForTypeCodeRequired (
			String typeCode) {

		return objectManager.objectClassForTypeCodeRequired (
			typeCode);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject) {

		return objectManager.objectPath (
			dataObject);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Optional<Record<?>> root) {

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
	public <ObjectType extends Record<?>>
	ObjectType update (
			ObjectType object) {

		return objectManager.update (
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
	ObjectHelper<?> objectHelperForTypeId (
			@NonNull Long typeId) {

		return objectManager.objectHelperForTypeId (
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
	Object dereference (
			Object object,
			String path,
			Map<String,Object> hints) {

		return objectManager.dereference (
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
