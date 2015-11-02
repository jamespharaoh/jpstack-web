package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;

import com.google.common.base.Optional;

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

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ObjectManager objectManager;

	@Inject
	Map<String,ConsoleHelper<?>> consoleHelpersByBeanName =
		Collections.emptyMap ();

	// state

	Map<Class<?>,ConsoleHelper<?>> consoleHelpersByObjectClass =
		new HashMap<Class<?>,ConsoleHelper<?>> ();

	@PostConstruct
	public
	void init () {

		// collect all object helpers

		Map<Class<?>,String> beanNamesByObjectClass =
			new HashMap<Class<?>,String> ();

		for (
			Map.Entry<String,ConsoleHelper<?>> entry
				: consoleHelpersByBeanName.entrySet ()
		) {

			String beanName =
				entry.getKey ();

			ConsoleHelper<?> consoleHelper =
				entry.getValue ();

			// check for duplicates

			String existingBeanName =
				beanNamesByObjectClass.get (
					consoleHelper.objectClass ());

			if (existingBeanName != null) {

				log.error (
					stringFormat (
						"Ignoring duplicated object helper class %s from %s, ",
						consoleHelper.objectClass ().getName (),
						beanName,
						"original from %s",
						existingBeanName));

				continue;

			}

			beanNamesByObjectClass.put (
				consoleHelper.objectClass (),
				beanName);

			// store it

			consoleHelpersByObjectClass.put (
				consoleHelper.objectClass (),
				consoleHelper);

			log.debug (
				stringFormat (
					"Adding object helper for %s from bean %s",
					consoleHelper.objectClass ().getName (),
					beanName));

		}

	}

	@Override
	public
	ConsoleHelper<?> getConsoleObjectHelper (
			Record<?> dataObject) {

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (
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
	ConsoleHelper<?> getConsoleObjectHelper (
			Class<?> objectClass) {

		Class<?> tempClass =
			objectClass;

		while (Record.class.isAssignableFrom (tempClass)) {

			if (consoleHelpersByObjectClass.containsKey (tempClass)) {

				ConsoleHelper<?> objectHelper =
					consoleHelpersByObjectClass.get (tempClass);

				return objectHelper;

			}

			tempClass =
				tempClass.getSuperclass ();

		}

		return null;

	}


	@Override
	public
	String tdForObject (
			Record<?> object,
			Record<?> assumedRoot,
			boolean mini,
			boolean link,
			int colspan) {

		if (object == null)
			return "<td>-</td>";

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (object);

		String path =
			objectManager.objectPath (
				object,
				assumedRoot,
				mini,
				false);

		StringBuilder stringBuilder =
			new StringBuilder ();

		if (link) {

			stringBuilder.append (
				stringFormat (
					"%s",
					Html.magicTd (
						requestContext.resolveLocalUrl (
							objectHelper.getDefaultLocalPath (object)),
						"main",
						colspan),
					"%h</td>",
					path));

		} else {

			stringBuilder.append (
				stringFormat (
					"<td>%h</td>\n",
					path));

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String tdForObjectMiniLink (
			Record<?> object) {

		return tdForObject (
			object,
			null,
			true,
			true,
			1);

	}

	@Override
	public
	String tdForObjectMiniLink (
			Record<?> object,
			Record<?> assumedRoot) {

		return tdForObject (
			object,
			assumedRoot,
			true,
			true,
			1);

	}

	@Override
	public
	String tdForObjectMiniLink (
			Record<?> object,
			int colspan) {

		return tdForObject (
			object,
			null,
			true,
			true,
			colspan);

	}

	@Override
	public
	String tdForObjectMiniLink (
			Record<?> object,
			Record<?> assumedRoot,
			int colspan) {

		return tdForObject (
			object,
			assumedRoot,
			true,
			true,
			colspan);

	}

	@Override
	public
	String tdForObjectLink (
			Record<?> object) {

		return tdForObject (
			object,
			null,
			false,
			true,
			1);

	}

	@Override
	public
	String tdForObjectLink (
			Record<?> object,
			Record<?> assumedRoot) {

		return tdForObject (
			object,
			assumedRoot,
			false,
			true,
			1);

	}

	@Override
	public
	String tdForObjectLink (
			Record<?> object,
			int colspan) {

		return tdForObject (
			object,
			null,
			false,
			true,
			colspan);

	}

	@Override
	public
	String tdForObjectLink (
			Record<?> object,
			Record<?> assumedRoot,
			int colspan) {

		return tdForObject (
			object,
			assumedRoot,
			false,
			true,
			colspan);

	}

	@Override
	public
	String htmlForObject (
			Record<?> object,
			Record<?> assumedRoot,
			boolean mini) {

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"%s.htmlForObject (%s, %s, %s)",
					getClass ().getName (),
					objectManager.objectPath (
						object,
						null,
						false,
						false),
					objectManager.objectPath (
						assumedRoot,
						null,
						false,
						false),
					Boolean.toString (mini)));

		}

		if (object == null) {
			return "NULL";
		}

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (object);

		return objectHelper.getHtml (
			object,
			Optional.<Record<?>>fromNullable (
				assumedRoot),
			mini);

	}

	@Override
	public
	String objectToSimpleHtml (
			Object object,
			Record<?> assumedRoot,
			boolean mini) {

		if (object instanceof Integer) {

			return Html.encode (
				object.toString ());

		}

		if (object instanceof Record) {

			Record<?> dataObject =
				(Record<?>) object;

			return htmlForObject (
				dataObject,
				assumedRoot,
				mini);

		}

		// TODO don't like this
		if (object == null)
			return "NULL";

		throw new IllegalArgumentException ();

	}

	@Override
	public
	boolean canView (
			Record<?> object) {

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"canView (%s) userId=%s",
					objectManager.objectPath (
						object,
						null,
						false,
						false),
					requestContext.userId ()));

		}

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (object);

		return objectHelper.canView (
			object);

	}

	@Override
	public
	String contextName (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (object);

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
			getConsoleObjectHelper (object);

		return requestContext.resolveContextUrl (
			objectHelper.getDefaultContextPath (object));

	}

	@Override
	public
	String localLink (
			Record<?> object) {

		ConsoleHelper<?> objectHelper =
			getConsoleObjectHelper (object);

		return requestContext.resolveLocalUrl (
			objectHelper.getDefaultLocalPath (object));

	}

	// delegate to objectManager

	@Override
	public
	ObjectHelper<?> objectHelperForTypeCode (
			String typeCode) {

		return objectManager
			.objectHelperForTypeCode (typeCode);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForClass (
			Class<?> objectClass) {

		return objectManager
			.objectHelperForClass (objectClass);

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
	Class<?> objectTypeCodeToClass (
			String typeCode) {

		return objectManager
			.objectTypeCodeToClass (typeCode);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject) {

		return objectManager
			.objectPath (dataObject);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Record<?> root) {

		return objectManager
			.objectPath (
				dataObject,
				root);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Record<?> root,
			boolean mini) {

		return objectManager.objectPath (
			dataObject,
			root,
			mini);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Record<?> assumedRoot,
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
	ObjectType insert (
			ObjectType object) {

		return objectManager.insert (
			object);

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
	ObjectHelper<?> objectHelperForObject (
			Record<?> object) {

		return objectManager.objectHelperForObject (
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
	public <ObjectType extends Record<?>>
	ObjectType findChildByCode (
			Class<ObjectType> objectClass,
			GlobalId parentGlobalId,
			String code) {

		return objectManager.findChildByCode (
			objectClass,
			parentGlobalId,
			code);

	}

	@Override
	public <ObjectType extends Record<?>>
	ObjectType findChildByCode (
			Class<ObjectType> objectClass,
			Record<?> parent,
			String code) {

		return objectManager.findChildByCode (
			objectClass,
			parent,
			code);

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
	int getObjectTypeId (
			Record<?> parentObject) {

		return objectManager
			.getObjectTypeId (parentObject);

	}

	@Override
	public
	int objectClassToTypeId (
			Class<?> objectClass) {

		return objectManager
			.objectClassToTypeId (objectClass);

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
			Integer typeId) {

		return objectManager
			.objectHelperForTypeId (typeId);

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
	ObjectHelper<?> objectHelperForObjectName (
			String objectName) {

		return objectManager.objectHelperForObjectName (
			objectName);

	}

	@Override
	public
	Object dereference (
			Object object,
			String path) {

		return objectManager.dereference (
			object,
			path);

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
			Record<?> root) {

		return objectManager.objectPathMini (
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

}
