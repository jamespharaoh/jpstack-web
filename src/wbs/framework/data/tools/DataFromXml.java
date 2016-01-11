package wbs.framework.data.tools;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.hyphenToCamel;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;
import static wbs.framework.utils.etc.Misc.toEnumGeneric;
import static wbs.framework.utils.etc.Misc.uncapitalise;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataContent;
import wbs.framework.data.annotations.DataIgnore;
import wbs.framework.data.annotations.DataInitMethod;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.utils.etc.BeanLogic;

/**
 * Automatically builds data objects from XML guided by annotations.
 *
 * TODO separate registry which builds worker
 * TODO read annotations first for efficiency
 */
@Accessors (fluent = true)
public
class DataFromXml {

	Map<String,List<DataClassInfo>>
		dataClassesMap =
			new HashMap<String,List<DataClassInfo>> ();

	Map<String,Map<String,?>> namedObjectCollections =
		new HashMap<String,Map<String,?>> ();

	public
	DataFromXml addNamedObjectCollection (
			String collectionName,
			Map<String,?> namedObjectCollection) {

		if (namedObjectCollection.containsKey (collectionName)) {

			throw new RuntimeException (
				stringFormat (
					"Named object collection \"%s\" added twice",
					collectionName));

		}

		namedObjectCollections.put (
			collectionName,
			namedObjectCollection);

		return this;

	}

	public
	DataFromXml registerBuilderClasses (
			List<Class<?>> builderClasses) {

		for (Class<?> builderClass
				: builderClasses) {

			registerBuilderClass (
				builderClass);

		}

		return this;

	}

	public
	DataFromXml registerBuilderClasses (
			Class<?>... builderClasses) {

		return registerBuilderClasses (
			Arrays.asList (
				builderClasses));

	}

	public
	DataFromXml registerBuilderClass (
			final Class<?> builderClass) {

		Provider<?> builderProvider =
			new Provider<Object> () {

			@Override
			public
			Object get () {

				try {

					return builderClass.newInstance ();

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Unable to instantiate builder class %s",
							builderClass.getName ()),
						exception);

				}

			}

		};

		registerBuilder (
			builderClass,
			builderProvider);

		return this;

	}

	public
	DataFromXml registerBuilder (
			Class<?> dataClass,
			Provider<?> builderProvider) {

		DataClass dataClassAnnotation =
			dataClass.getAnnotation (
				DataClass.class);

		if (dataClassAnnotation == null) {

			throw new RuntimeException (
				stringFormat (
					"Builder class %s has no @DataClass annotation",
					dataClass.getName ()));

		}

		String elementName =
			ifNull (
				nullIfEmptyString (
					dataClassAnnotation.value ()),
				camelToHyphen (
					dataClass.getSimpleName ()));

		Field parentField =
			findParentField (dataClass);

		Class<?> parentClass =
			parentField != null
				? parentField.getType ()
				: Object.class;

		// add to map

		List<DataClassInfo> dataClassInfos =
			dataClassesMap.get (
				elementName);

		if (dataClassInfos == null) {

			dataClassInfos =
				new ArrayList<DataClassInfo> ();

			dataClassesMap.put (
				elementName,
				dataClassInfos);

		}

		dataClassInfos.add (
			new DataClassInfo ()

			.parentClass (
				parentClass)

			.dataClass (
				dataClass)

			.provider (
				builderProvider)

		);

		return this;

	}

	Field findParentField (
			Class<?> dataClass) {

		Field parentField = null;

		for (
			Field field
				: dataClass.getDeclaredFields ()
		) {

			DataParent dataParentAnnotation =
				field.getAnnotation (DataParent.class);

			if (dataParentAnnotation == null)
				continue;

			if (parentField != null)
				throw new RuntimeException ();

			parentField =
				field;

		}

		return parentField;

	}

	public
	Object readInputStream (
			InputStream inputStream,
			String filename,
			Collection<Object> parents) {

		SAXReader saxReader =
			new SAXReader ();

		Document document;

		try {

			document =
				saxReader.read (
					inputStream,
					filename);

		} catch (DocumentException exception) {

			throw new RuntimeException (exception);

		}

		try {

			return new ElementBuilder ()

				.element (
					document.getRootElement ())

				.parents (
					parents)

				.contextString (
					filename)

				.build ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error reading %s",
					filename),
				exception);

		}

	}

	public
	Object readClasspath (
			List<Object> parents,
			String filename) {

		SAXReader saxReader =
			new SAXReader ();

		InputStream inputStream =
			getClass ().getResourceAsStream (
				filename);

		if (inputStream == null) {

			throw new RuntimeException (
				stringFormat (
					"Classpath resource %s not found",
					filename));

		}

		Document document;

		try {

			document =
				saxReader.read (
					getClass ().getResource (filename));

		} catch (DocumentException exception) {

			throw new RuntimeException (exception);

		}

		return new ElementBuilder ()

			.element (
				document.getRootElement ())

			.parents (
				parents)

			.contextString (
				filename)

			.build ();

	}

	public
	Object readFilename (
			String filename) {

		SAXReader saxReader =
			new SAXReader ();

		Document document;

		try {

			document =
				saxReader.read (
					new File (filename));

		} catch (DocumentException exception) {

			throw new RuntimeException (exception);

		}

		try {

			return new ElementBuilder ()

				.element (
					document.getRootElement ())

				.parents (
					Collections.emptyList ())

				.contextString (
					filename)

				.build ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error reading %s",
					filename),
				exception);

		}

	}

	@Accessors (fluent = true)
	class ElementBuilder {

		@Getter @Setter
		Element element;

		@Getter @Setter
		Iterable<Object> parents;

		@Getter @Setter
		String contextString;

		Object object;

		Set<String> matchedAttributes =
			new HashSet<String> ();

		Set<String> matchedElementNames =
			new HashSet<String> ();

		Object build () {

			Iterator<Object> parentsIterator =
				parents.iterator ();

			Class<?> parentClass =
				parentsIterator.hasNext ()
					? parentsIterator.next ().getClass ()
					: Object.class;

			// find the appropriate builder

			List<DataClassInfo> dataClassInfosForElementName =
				dataClassesMap.get (element.getName ());

			if (dataClassInfosForElementName == null) {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map <%s>",
						element.getName ()));

			}

			List<DataClassInfo> matchingDataClassInfos =
				new ArrayList<DataClassInfo> ();

			for (
				DataClassInfo dataClassInfo :
					dataClassInfosForElementName
			) {

				if (! dataClassInfo.parentClass.isAssignableFrom (
						parentClass))
					continue;

				matchingDataClassInfos.add (
					dataClassInfo);

			}

			if (matchingDataClassInfos.isEmpty ()) {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map <%s> with parent %s",
						element.getName (),
						parentClass.getName ()));

			}

			if (matchingDataClassInfos.size () > 1) {

				List<String> matchingDataClassNames =
					new ArrayList<String> ();

				for (DataClassInfo matchingDataClassInfo
						: matchingDataClassInfos) {

					matchingDataClassNames.add (
						matchingDataClassInfo.dataClass ().getName ());

				}

				throw new RuntimeException (
					stringFormat (
						"Multiple mappings for <%s> with parent %s: %s",
						element.getName (),
						parentClass.getName (),
						joinWithSeparator (
							", ",
							matchingDataClassNames)));

			}

			Provider<?> builder =
				matchingDataClassInfos.get (0).provider ();

			// build it

			object =
				builder.get ();

			for (
				Field field
					: object.getClass ().getDeclaredFields ()
			) {

				buildField (
					field,
					contextString);

			}

			// check for unmatched attributes

			for (
				Object attributeObject
					: element.attributes ()
			) {

				Attribute attribute =
					(Attribute)
					attributeObject;

				String attributeName =
					attribute.getName ();

				if (matchedAttributes.contains (
						attributeName))
					continue;

				throw new RuntimeException (
					stringFormat (
						"Don't understand attribute %s on <%s>, using %s",
						attributeName,
						element.getName (),
						object.getClass ().getSimpleName ()));

			}

			// check for unmatched elements

			for (
				Object childElementObject
					: element.elements ()
			) {

				Element childElement =
					(Element) childElementObject;

				if (matchedElementNames.contains (
						childElement.getName ()))
					continue;

				throw new RuntimeException (
					stringFormat (
						"Don't understand element <%s> in <%s>, using %s",
						childElement.getName (),
						element.getName (),
						object.getClass ().getSimpleName ()));

			}

			// run init method

			for (
				Method method
					: object.getClass ().getMethods ()
			) {

				DataInitMethod dataInitMethodAnnotation =
					method.getAnnotation (DataInitMethod.class);

				if (dataInitMethodAnnotation == null)
					continue;

				if (method.getParameterTypes ().length > 0)
					throw new RuntimeException ();

				try {

					method.invoke (
						object);

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Error invoking data init method %s.%s",
							object.getClass ().getName (),
							method.getName ()),
						exception);

				}

			}

			return object;

		}

		void buildField (
				Field field,
				String contextString) {

			for (
				Annotation annotation
					: field.getAnnotations ()
			) {

				if (annotation instanceof DataAttribute) {

					buildAttributeField (
						field,
						(DataAttribute) annotation,
						contextString);

				}

				if (annotation instanceof DataContent) {

					buildContentField (
						field,
						(DataContent) annotation,
						contextString);

				}

				if (annotation instanceof DataChild) {

					buildChildField (
						field,
						(DataChild) annotation,
						contextString);

				}

				if (annotation instanceof DataChildren) {

					buildChildrenField (
						field,
						(DataChildren) annotation);

				}

				if (annotation instanceof DataChildrenIndex) {

					buildChildrenIndexField (
						field,
						(DataChildrenIndex) annotation);

				}

				if (annotation instanceof DataParent) {

					if (! parents.iterator ().hasNext ())
						throw new RuntimeException ();

					BeanLogic.set (
						object,
						field.getName (),
						parents.iterator ().next ());

				}

				if (annotation instanceof DataAncestor) {

					for (Object ancestor : parents) {

						if (! field.getType ().isInstance (ancestor))
							continue;

						BeanLogic.set (
							object,
							field.getName (),
							ancestor);

						break;

					}

				}

				if (annotation instanceof DataIgnore) {

					matchedElementNames.add (
						camelToHyphen (
							field.getName ()));

				}

			}

		}

		void buildAttributeField (
				@NonNull Field field,
				@NonNull DataAttribute dataAttributeAnnotation,
				@NonNull String contextString) {

			String attributeName =
				ifNull (
					nullIfEmptyString (
						dataAttributeAnnotation.name ()),
					camelToHyphen (
						field.getName ()));

			String attributeValue =
				element.attributeValue (
					attributeName);

			if (attributeValue == null) {

				if (dataAttributeAnnotation.required ()) {

					throw new RuntimeException (
						stringFormat (
							"Required attribute %s of <%s> missing at %s",
							attributeName,
							element.getName (),
							contextString));

				}

				return;

			}

			matchedAttributes.add (
				attributeName);

			if (! dataAttributeAnnotation.collection ().isEmpty ()) {

				Map<String,?> collection =
					namedObjectCollections.get (
						dataAttributeAnnotation.collection ());

				if (collection == null) {

					throw new RuntimeException (
						stringFormat (
							"Named collection %s doesn't exist for %s.%s",
							dataAttributeAnnotation.collection (),
							object.getClass ().getSimpleName (),
							field.getName ()));

				}

				Object namedObject =
					collection.get (attributeValue);

				if (namedObject == null) {

					throw new RuntimeException (
						stringFormat (
							"Named object %s not found in collection %s",
							attributeValue,
							dataAttributeAnnotation.collection ()));

				}

				BeanLogic.set (
					object,
					field.getName (),
					namedObject);

			} else if (field.getType () == String.class) {

				BeanLogic.set (
					object,
					field.getName (),
					attributeValue);

			} else if (field.getType () == Integer.class) {

				BeanLogic.set (
					object,
					field.getName (),
					Integer.parseInt (attributeValue));

			} else if (field.getType () == Long.class) {

				BeanLogic.set (
					object,
					field.getName (),
					Long.parseLong (attributeValue));

			} else if (field.getType () == Boolean.class) {

				BeanLogic.set (
					object,
					field.getName (),
					stringToBoolean (attributeValue));

			} else if (field.getType ().isEnum ()) {

				Enum<?> enumValue =
					toEnumGeneric (
						field.getType (),
						hyphenToCamel (
							attributeValue));

				BeanLogic.set (
					object,
					field.getName (),
					enumValue);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map attribute to %s ",
						field.getType ().getName (),
						"at %s.%s",
						object.getClass ().getSimpleName (),
						field.getName ()));

			}

		}

		void buildContentField (
				@NonNull Field field,
				@NonNull DataContent dataContentAnnotation,
				@NonNull String contextString) {

			String stringValue =
				element.getTextTrim ();

			if (field.getType () == String.class) {

				BeanLogic.set (
					object,
					field.getName (),
					stringValue);

			} else {

				throw new RuntimeException ();

			}

		}

		void buildChildField (
				Field field,
				DataChild dataChildAnnotation,
				String filename) {

			String childElementName =
				camelToHyphen (
					field.getName ());

			Element childElement =
				element.element (
					childElementName);

			if (childElement == null)
				return;

			matchedElementNames.add (
				childElementName);

			Object nextParent =
				object;

			Iterable<Object> nextParents =
				Iterables.concat (
					Collections.singletonList (nextParent),
					parents);

			Object child =
				new ElementBuilder ()

				.element (
					childElement)

				.parents (
					nextParents)

				.contextString (
					filename)

				.build ();

			BeanLogic.set (
				object,
				field.getName (),
				child);

		}

		void buildChildrenField (
				Field field,
				DataChildren dataChildrenAnnotation) {

			if (
				! dataChildrenAnnotation.direct ()
				&& ! dataChildrenAnnotation.childElement ().isEmpty ()
				&& (
					field.getType () != Map.class
					&& field.getType () != List.class
				)
			) {

				throw new RuntimeException (
					stringFormat (
						"Don't specify childElement for indirect children, at ",
						"%s.%s",
						field.getDeclaringClass ().getSimpleName (),
						field.getName ()));

			}

			// find the element which contains the children

			Element childrenElement;

			if (dataChildrenAnnotation.direct ()) {

				childrenElement =
					element;

				if (childrenElement == null)
					return;

			} else {

				String childrenElementName =
					! dataChildrenAnnotation.childrenElement ().isEmpty ()
						? dataChildrenAnnotation.childrenElement ()
						: camelToHyphen (field.getName ());

				List<?> childrenElementObjects =
					element.elements (childrenElementName);

				if (childrenElementObjects.isEmpty ())
					return;

				if (childrenElementObjects.size () > 1)
					throw new RuntimeException ();

				matchedElementNames.add (
					childrenElementName);

				childrenElement =
					(Element)
					childrenElementObjects.get (0);

			}

			// work out parents for recursive call

			Iterable<Object> nextParents;

			if (! dataChildrenAnnotation.surrogateParent ().isEmpty ()) {

				nextParents =
					Iterables.concat (
						ImmutableList.<Object>of (
							BeanLogic.get (
								object,
								dataChildrenAnnotation.surrogateParent ()),
							object),
						parents);

			} else {

				nextParents =
					Iterables.concat (
						Collections.singletonList (object),
						parents);

			}

			// collect children

			List<Object> children =
				new ArrayList<Object> ();

			List<?> childElementObjects =
				! dataChildrenAnnotation.childElement ().isEmpty ()
					? childrenElement.elements (
						dataChildrenAnnotation.childElement ())
					: childrenElement.elements ();

			Set<String> newlyMatchedElementNames =
				new HashSet<String> ();

			for (
				Object childElementObject
					: childElementObjects
			) {

				Element childElement =
					(Element) childElementObject;

				if (dataChildrenAnnotation.direct ()) {

					if (
						contains (
							matchedElementNames,
							childElement.getName ())
					) {
						continue;
					}

					newlyMatchedElementNames.add (
						childElement.getName ());

				}

				if (field.getType () == Map.class) {

					String entryKey =
						childElement.attributeValue (
							dataChildrenAnnotation.keyAttribute ());

					if (entryKey == null)
						throw new RuntimeException ();

					String entryValue =
						childElement.attributeValue (
							dataChildrenAnnotation.valueAttribute ());

					if (entryValue == null)
						throw new RuntimeException ();

					children.add (
						Pair.of (
							entryKey,
							entryValue));

				} else if (
					field.getType () == List.class
					&& ! dataChildrenAnnotation.valueAttribute ().isEmpty ()
				) {

					Type genericType =
						field.getGenericType ();

					Class<?> itemClass;

					if (genericType instanceof ParameterizedType) {

						ParameterizedType parameterizedType =
							(ParameterizedType) genericType;

						itemClass =
							(Class<?>)
							parameterizedType.getActualTypeArguments () [0];

					} else {

						itemClass = null;

					}

					String stringValue =
						childElement.attributeValue (
							dataChildrenAnnotation.valueAttribute ());

					if (stringValue == null) {

						throw new RuntimeException (
							stringFormat (
								"No attribute %s on <%s>",
								dataChildrenAnnotation.valueAttribute (),
								childElement.getName ()));

					}

					Object value;

					if (itemClass == Integer.class) {

						value =
							Integer.parseInt (
								stringValue);

					} else {

						value =
							stringValue;

					}

					children.add (
						value);

				} else {

					children.add (
						new ElementBuilder ()

						.element (
							childElement)

						.parents (
							nextParents)

						.contextString (
							contextString)

						.build ());

				}

			}

			matchedElementNames.addAll (
				newlyMatchedElementNames);

			// set them

			if (field.getType () == Map.class) {

				ImmutableMap.Builder<Object,Object> mapBuilder =
					ImmutableMap.<Object,Object>builder ();

				for (
					Object pairObject
						: children
				) {

					Pair<?,?> pair =
						(Pair<?,?>) pairObject;

					mapBuilder.put (
						pair.getLeft (),
						pair.getRight());

				}

				BeanLogic.set (
					object,
					field.getName (),
					mapBuilder.build ());

			} else {

				BeanLogic.set (
					object,
					field.getName (),
					children);

			}

		}

		void buildChildrenIndexField (
				Field field,
				DataChildrenIndex dataChildrenIndexAnnotation) {

			Matcher matcher =
				childrenIndexPattern.matcher (field.getName ());

			if (! matcher.matches ())
				throw new RuntimeException ();

			String childrenFieldName =
				matcher.group (1);

			String indexFieldName =
				uncapitalise (matcher.group (2));

			Map<Object,Object> childrenIndex =
				new LinkedHashMap<Object,Object> ();

			List<?> children =
				(List<?>)
				BeanLogic.get (
					object,
					childrenFieldName);

			for (Object child : children) {

				Object index =
					BeanLogic.get (
						child,
						indexFieldName);

				if (index == null)
					continue;

				childrenIndex.put (
					index,
					child);

			}

			BeanLogic.set (
				object,
				field.getName (),
				childrenIndex);

		}

	}

	static
	Pattern childrenIndexPattern =
		Pattern.compile ("(.+)By(.+)");

	@Accessors (fluent = true)
	@Data
	static
	class DataClassInfo {

		Class<?> parentClass;
		Class<?> dataClass;
		Provider<?> provider;

	}

}
