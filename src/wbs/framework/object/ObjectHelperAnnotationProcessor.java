package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.split;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringFormatArray;
import static wbs.framework.utils.etc.Misc.uncapitalise;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.RootEntity;
import wbs.framework.entity.annotations.TypeEntity;

import com.google.common.collect.ImmutableList;

@SupportedAnnotationTypes ({
	"wbs.framework.entity.annotations.CommonEntity",
	"wbs.framework.entity.annotations.EphemeralEntity",
	"wbs.framework.entity.annotations.MajorEntity",
	"wbs.framework.entity.annotations.MinorEntity",
	"wbs.framework.entity.annotations.RootEntity",
	"wbs.framework.entity.annotations.TypeEntity"})
@SupportedSourceVersion (
	SourceVersion.RELEASE_6)
public
class ObjectHelperAnnotationProcessor
	extends AbstractProcessor {

	@Override
	public
	boolean process (
			Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnvironment) {

		try {

			for (
				Class<? extends Annotation> annotationClass
					: annotationClasses
			) {

				for (
					Element element
						: roundEnvironment.getElementsAnnotatedWith (
							annotationClass)
				) {

					TypeElement typeElement =
						(TypeElement)
						element;

					processTypeElement (
						typeElement);

				}

			}

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		}

		return true;

	}

	void processTypeElement (
			TypeElement typeElement)
		throws IOException {

		// work out name

		String recordName =
			typeElement.getSimpleName ().toString ();

		String objectName;

		if (recordName.endsWith ("Rec")) {

			objectName =
				uncapitalise (
					recordName.substring (
						0,
						recordName.length () - 3));

		} else if (recordName.endsWith ("View")) {

			objectName =
				uncapitalise (
					recordName.substring (
						0,
						recordName.length () - 4));

		} else {

			throw new RuntimeException (
				recordName);

		}

		// work out packages

		PackageElement modelPackageElement =
			(PackageElement)
			typeElement.getEnclosingElement ();

		if (
			notEqual (
				modelPackageElement.getSimpleName ().toString (),
				"model")
		) {

			throw new RuntimeException ();

		}

		String modelPackageName =
			modelPackageElement.getQualifiedName ().toString ();

		List<String> modelPackageNameParts =
			split (
				modelPackageName,
				"\\.");

		String pluginPackageName =
			joinWithSeparator (
				".",
				modelPackageNameParts.subList (
					0,
					modelPackageNameParts.size () - 1));

		String consolePackageName =
			stringFormat (
				"%s.console",
				pluginPackageName);

		// work out other names

		String objectHelperName =
			stringFormat (
				"%sObjectHelper",
				capitalise (objectName));

		String objectHelperMethodsName =
			stringFormat (
				"%sObjectHelperMethods",
				capitalise (objectName));

		String daoName =
			stringFormat (
				"%sDao",
				capitalise (objectName));

		String daoMethodsName =
			stringFormat (
				"%sDaoMethods",
				capitalise (objectName));

		String consoleHelperName =
			stringFormat (
				"%sConsoleHelper",
				capitalise (objectName));

		// check what enclosed elements there are

		boolean gotObjectHelperMethods =
			false;

		boolean gotDaoMethods =
			false;

		for (Element enclosedElement
				: typeElement.getEnclosedElements ()) {

			String enclosedElementName =
				enclosedElement.getSimpleName ().toString ();

			if (
				equal (
					enclosedElementName,
					objectHelperMethodsName)
			) {

				gotObjectHelperMethods = true;

			}

			if (
				equal (
					enclosedElementName,
					daoMethodsName)
			) {

				gotDaoMethods = true;

			}

		}

		// write object helper

		InterfaceWriter objectHelperWriter =
			new InterfaceWriter ()

			.packageName (
				modelPackageName)

			.name (
				objectHelperName);

		if (gotObjectHelperMethods) {

			objectHelperWriter

				.addImport (
					"%s.%s.%s",
					modelPackageName,
					recordName,
					objectHelperMethodsName)

				.addInterface (
					"%s",
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			objectHelperWriter

				.addImport (
					"%s.%s.%s",
					modelPackageName,
					recordName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		objectHelperWriter

			.addImport (
				"wbs.framework.object.ObjectHelper")

			.addInterface (
				"ObjectHelper<%s>",
				recordName);

		objectHelperWriter
			.build ();

		// write dao

		InterfaceWriter daoWriter =
			new InterfaceWriter ()

			.packageName (
				modelPackageName)

			.name (
				daoName);

		if (gotDaoMethods) {

			daoWriter

				.addImport (
					"%s.%s.%s",
					modelPackageName,
					recordName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		daoWriter
			.build ();

		// write console helper

		InterfaceWriter consoleHelperWriter =
			new InterfaceWriter ()

			.packageName (
				consolePackageName)

			.name (
				consoleHelperName)

			.addImport (
				"%s.%s",
				modelPackageName,
				recordName);

		if (gotObjectHelperMethods) {

			consoleHelperWriter

				.addImport (
					"%s.%s.%s",
					modelPackageName,
					recordName,
					objectHelperMethodsName)

				.addInterface (
					"%s",
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			consoleHelperWriter

				.addImport (
					"%s.%s.%s",
					modelPackageName,
					recordName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		consoleHelperWriter

			.addImport (
				"wbs.console.helper.ConsoleHelper")

			.addInterface (
				"ConsoleHelper<%s>",
				recordName);

		consoleHelperWriter
			.build ();

	}

	@Accessors (fluent = true)
	class InterfaceWriter {

		@Getter @Setter
		String packageName;

		@Getter @Setter
		String name;

		List<String> imports =
			new ArrayList<String> ();

		List<String> interfaces =
			new ArrayList<String> ();

		public
		InterfaceWriter addImport (
				String... args) {

			imports.add (
				stringFormatArray (args));

			return this;

		}

		public
		InterfaceWriter addInterface (
				String... args) {

			interfaces.add (
				stringFormatArray (args));

			return this;

		}

		public
		void build ()
			throws IOException {

			JavaFileObject javaFileObject =
				processingEnv

				.getFiler ()

				.createSourceFile (
					stringFormat (
						"%s.%s",
						packageName,
						name));

			@Cleanup
			Writer writer =
				javaFileObject.openWriter ();

			writer.write (
				stringFormat (

					"\n",

					"package %s;\n",
					packageName,

					"\n"));

			if (! imports.isEmpty ()) {

				for (String importValue
						: imports) {

					writer.write (
						stringFormat (
							"import %s;\n",
							importValue));

				}

				writer.write (
					stringFormat (
						"\n"));

			}

			writer.write (
				stringFormat (
					"public\n",
					"interface %s ",
					name));

			if (! interfaces.isEmpty ()) {

				writer.write (
					stringFormat (
						"\n\textends %s",
						joinWithSeparator (
							",\n\t\t",
							interfaces)));

			}

			writer.write (
				stringFormat (
					"{\n",
					"\n",
					"}\n",
					"\n"));

			writer.close ();

		}

	}

	static
	List<Class<? extends Annotation>> annotationClasses =
		ImmutableList.<Class<? extends Annotation>>of (
			CommonEntity.class,
			EphemeralEntity.class,
			MajorEntity.class,
			MinorEntity.class,
			RootEntity.class,
			TypeEntity.class);

}
