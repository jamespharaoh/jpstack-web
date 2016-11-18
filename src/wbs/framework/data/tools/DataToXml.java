package wbs.framework.data.tools;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringFormatObsolete;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.io.output.FileWriterWithEncoding;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataReference;
import wbs.utils.etc.PropertyUtils;
import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
public
class DataToXml {

	public
	void write (
			@NonNull Writer writer,
			@NonNull Object object) {

		Document document =
			DocumentHelper.createDocument ();

		createElement (
			document,
			object);

		OutputFormat format =
			OutputFormat.createPrettyPrint ();

		XMLWriter xmlWriter =
			new XMLWriter (
				writer,
				format);

		try {

			xmlWriter.write (
				document);

			xmlWriter.flush ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public
	void writeToFile (
			@NonNull String filename,
			@NonNull Object object) {

		try {

			write (
				new FileWriterWithEncoding (
					new File (
						filename),
					"utf-8"),
				object);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public
	String writeToString (
			@NonNull Object object) {

		StringWriter stringWriter =
			new StringWriter ();

		write (
			stringWriter,
			object);

		return stringWriter.toString ();

	}

	void createElement (
			@NonNull Branch parent,
			@NonNull Object object) {

		if (object instanceof String) {

			parent
				.addElement ("string")
				.addAttribute ("value", object.toString ());

			return;

		}

		if (object instanceof List) {

			Element listElement =
				parent.addElement ("list");

			for (
				Object item
					: (List<?>) object
			) {

				createElement (
					listElement,
					item);

			}

			return;

		}

		if (object instanceof Class) {

			Class<?> classObject =
				(Class<?>) object;

			parent

				.addElement (
					"class")

				.addAttribute (
					"name",
					classObject.getName ());

			return;

		}

		if (object instanceof Properties) {

			Properties propertiesObject =
				(Properties) object;

			Element propertiesElement =
				parent.addElement ("properties");

			for (
				Map.Entry<?,?> propertyEntry
					: propertiesObject.entrySet ()
			) {

				propertiesElement

					.addElement (
						"property")

					.addAttribute (
						"name",
						(String) propertyEntry.getKey ())

					.addAttribute (
						"value",
						(String) propertyEntry.getValue ());

			}

			return;

		}

		DataClass dataClassAnnotation =
			object.getClass ().getAnnotation (DataClass.class);

		if (dataClassAnnotation == null) {

			parent

				.addElement (
					"unknown-object")

				.addAttribute (
					"package",
					object.getClass ().getPackage ().getName ())

				.addAttribute (
					"class",
					object.getClass ().getSimpleName ())

				.addAttribute (
					"identity",
					Integer.toString (
						System.identityHashCode (object)));

			return;

			/*
			throw new NullPointerException (sf (
				"No @DataClass annotation for %s",
				object.getClass ().getName ()));
			*/

		}

		String elementName =
			ifNull (
				nullIfEmptyString (
					dataClassAnnotation.value ()),
				camelToHyphen (
					object.getClass ().getSimpleName ()));

		Element element =
			parent.addElement (
				elementName);

		for (
			Field field
				: object.getClass ().getDeclaredFields ()
		) {

			for (Annotation annotation
					: field.getAnnotations ()) {

				if (annotation instanceof DataName) {

					String attributeName =
						camelToHyphen (
							field.getName ());

					Object attributeValueObject =
						PropertyUtils.get (
							object,
							field.getName ());

					String attributeValue =
						(String) attributeValueObject;

					element.addAttribute (
						attributeName,
						attributeValue);

				}

				if (annotation instanceof DataAttribute) {

					String attributeName =
						camelToHyphen (
							field.getName ());

					Object attributeValueObject =
						PropertyUtils.get (
							object,
							field.getName ());

					if (attributeValueObject == null)
						continue;

					String attributeValue =
						toString (
							attributeValueObject.toString ());

					element.addAttribute (
						attributeName,
						attributeValue);

				}

				if (annotation instanceof DataChild) {

					DataChild dataChildAnnotation =
						(DataChild)
						annotation;

					String childElementName =
						ifNull (
							nullIfEmptyString (
								dataChildAnnotation.name ()),
							camelToHyphen (
								field.getName ()));

					Object childValueObject =
						PropertyUtils.get (
							object,
							field.getName ());

					if (
						isNull (
							childValueObject)
					) {
						continue;
					}

					String childValueString =
						toString (
							childValueObject);

					Element childElement =
						element.addElement (
							childElementName);

					childElement.setText (
						childValueString);

				}

				if (annotation instanceof DataChildren) {

					Object children =
						PropertyUtils.get (
							object,
							field.getName ());

					if (
						isNotNull (
							children)
					) {

						createChildren (
							field,
							(DataChildren)
								annotation,
							element,
							children);

					}

				}

			}

		}

		for (
			Field field
				: object.getClass ().getDeclaredFields ()
		) {

			for (Annotation annotation
					: field.getAnnotations ()) {

				if (annotation instanceof DataReference) {

					String attributeName =
						camelToHyphen (
							field.getName ());

					Object referencedObject =
						PropertyUtils.get (
							object,
							field.getName ());

					if (referencedObject == null)
						continue;

					String referencedObjectName =
						objectName (referencedObject);

					element.addAttribute (
						attributeName,
						referencedObjectName);

				}

			}

		}

	}

	void createChildren (
			@NonNull Field field,
			@NonNull DataChildren dataChildrenAnnotation,
			@NonNull Element element,
			@NonNull Object children) {

		String childrenElementName =
			ifNull (
				nullIfEmptyString (
					dataChildrenAnnotation.childrenElement ()),
				camelToHyphen (
					field.getName ()));

		if (children instanceof Map) {

			Map <?,?> childrenMap =
				(Map <?,?>) children;

			if (
				mapIsEmpty (
					childrenMap)
			) {
				return;
			}

			Element containingElement =
				ifThenElse (
					dataChildrenAnnotation.direct (),
					() -> element,
					() -> element.addElement (
						childrenElementName));

			for (
				Map.Entry<?,?> entry
					: childrenMap.entrySet ()
			) {

				Element entryElement =
					containingElement.addElement (
						"entry");

				if (entry.getKey () instanceof String) {

					entryElement.addAttribute (
						"key",
						(String) entry.getKey ());

				} else {

					Element keyElement =
						entryElement.addElement (
							"key");

					createElement (
						keyElement,
						entry.getKey ());

				}

				if (entry.getValue () instanceof String) {

					entryElement.addAttribute (
						"value",
						(String)
						entry.getValue ());

				} else if (entry.getValue () instanceof Boolean) {

					entryElement.addAttribute (
						"value",
						booleanToYesNo (
							(Boolean)
							entry.getValue ()));

				} else {

					Element valueElement =
						entryElement.addElement (
							"value");

					createElement (
						valueElement,
						entry.getValue ());

				}

			}

		} else if (
			children instanceof List
			|| children instanceof Set
		) {

			Collection <?> childrenCollection =
				(Collection <?>)
				children;

			if (
				collectionIsEmpty (
					childrenCollection)
			) {
				return;
			}

			Element containingElement =
				ifThenElse (
					dataChildrenAnnotation.direct (),
					() -> element,
					() -> element.addElement (
						childrenElementName));

			for (
				Object child
					: childrenCollection
			) {

				createElement (
					containingElement,
					child);

			}

		} else {

			throw new RuntimeException (
				stringFormatObsolete (
					"Don't know how to handle collection of type %s",
					children.getClass ()));

		}

	}

	String objectName (
			Object object) {

		for (Field field
				: object.getClass ().getDeclaredFields ()) {

			for (Annotation annotation
					: field.getAnnotations ()) {

				if (! (annotation instanceof DataName))
					continue;

				Object nameValueObject =
					PropertyUtils.get (
						object,
						field.getName ());

				String nameValue =
					(String) nameValueObject;

				return nameValue;

			}
		}

		throw new RuntimeException ();

	}

	String toString (
			Object object) {

		if (object instanceof String)
			return (String) object;

		if (object instanceof Class)
			return ((Class<?>) object).getName ();

		return object.toString ();

	}

}
