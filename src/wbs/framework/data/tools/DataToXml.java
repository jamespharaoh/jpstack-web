package wbs.framework.data.tools;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;

import java.io.File;
import java.io.FileWriter;
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
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.utils.etc.RuntimeIoException;

@Accessors (fluent = true)
public
class DataToXml {

	public
	void write (
			@NonNull Writer writer,
			@NonNull Object object)
		throws IOException {

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

		xmlWriter.write (
			document);

	}

	public
	void writeToFile (
			@NonNull String filename,
			@NonNull Object object)
		throws IOException {

		write (
			new FileWriter (
				new File (filename)),
			object);

	}

	public
	String writeToString (
			@NonNull Object object) {

		try {

			StringWriter stringWriter =
				new StringWriter ();

			write (
				stringWriter,
				object);

			return stringWriter.toString ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	void createElement (
			Branch parent,
			Object object) {

		if (object instanceof String) {

			parent
				.addElement ("string")
				.addAttribute ("value", object.toString ());

			return;

		}

		if (object instanceof List) {

			Element listElement =
				parent.addElement ("list");

			for (Object item
					: (List<?>) object) {

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

		for (Field field
				: object.getClass ().getDeclaredFields ()) {

			for (Annotation annotation
					: field.getAnnotations ()) {

				if (annotation instanceof DataName) {

					String attributeName =
						camelToHyphen (
							field.getName ());

					Object attributeValueObject =
						BeanLogic.get (
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
						BeanLogic.get (
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
						BeanLogic.get (
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
						BeanLogic.get (
							object,
							field.getName ());

					String childrenElementName =
						camelToHyphen (
							field.getName ());

					createChildren (
						element,
						childrenElementName,
						children);

				}

			}

		}

		for (Field field
				: object.getClass ().getDeclaredFields ()) {

			for (Annotation annotation
					: field.getAnnotations ()) {

				if (annotation instanceof DataReference) {

					String attributeName =
						camelToHyphen (
							field.getName ());

					Object referencedObject =
						BeanLogic.get (
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
			Element element,
			String childrenElementName,
			Object children) {

		if (children == null) {

			doNothing ();

		} else if (children instanceof Map) {

			Map<?,?> childrenMap =
				(Map<?,?>) children;

			if (childrenMap.isEmpty ())
				return;

			Element childrenElement =
				element.addElement (
					childrenElementName);

			for (Map.Entry<?,?> entry
					: childrenMap.entrySet ()) {

				Element entryElement =
					childrenElement.addElement ("entry");

				if (entry.getKey () instanceof String) {

					entryElement.addAttribute (
						"key",
						(String) entry.getKey ());

				} else {

					Element keyElement =
						entryElement.addElement ("key");

					createElement (
						keyElement,
						entry.getKey ());

				}

				if (entry.getValue () instanceof String) {

					entryElement.addAttribute (
						"value",
						(String) entry.getValue ());

				} else {

					Element valueElement =
						entryElement.addElement ("value");

					createElement (
						valueElement,
						entry.getValue ());

				}

			}

		} else if (
			children instanceof List
			|| children instanceof Set) {

			Collection<?> childrenCollection =
				(Collection<?>) children;

			if (childrenCollection.isEmpty ())
				return;

			Element childrenElement =
				element.addElement (
					childrenElementName);

			for (Object child
					: childrenCollection) {

				createElement (
					childrenElement,
					child);

			}

		} else {

			throw new RuntimeException (
				stringFormat (
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
					BeanLogic.get (
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
