package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.apache.commons.io.FileUtils;

import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.entity.meta.CodeFieldSpec;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentFieldSpec;

import com.google.common.collect.ImmutableList;

public
class ModelRecordGenerator {

	// dependencies

	@Inject
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ModelWriter
	Map<Class<?>,Provider<Object>> modelWriterProviders;

	// state

	Builder modelWriter;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		createModelWriter ();

	}

	// implementation

	private
	void createModelWriter () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelWriterEntry
				: modelWriterProviders.entrySet ()
		) {

			builderFactory.addBuilder (
				modelWriterEntry.getKey (),
				modelWriterEntry.getValue ());

		}

		modelWriter =
			builderFactory.create ();

	}

	@SneakyThrows (IOException.class)
	public
	void generateModelRecords (
			List<String> params) {

		for (
			ModelMetaSpec modelSpec
				: modelMetaLoader.modelSpecs ().values ()
		) {

			PluginModelSpec pluginModel =
				modelSpec.pluginModel ();

			PluginSpec plugin =
				pluginModel.plugin ();

			if (modelSpec.type () == null) {
				continue;
			}

			System.out.println (
				stringFormat (
					"GENERATE: %s",
					modelSpec.name ()));

			generateModelRecord (
				plugin,
				pluginModel,
				modelSpec);

		}

	}

	private
	void generateModelRecord (
			PluginSpec plugin,
			PluginModelSpec pluginModel,
			ModelMetaSpec modelMeta)
		throws IOException {

		String className =
			stringFormat (
				"%sRec",
				capitalise (
					modelMeta.name ()));

		String directory =
			stringFormat (
				"work/generated/%s/model",
				plugin.packageName ().replace ('.', '/'));

		FileUtils.forceMkdir (
			new File (directory));

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				className);

		@Cleanup
		OutputStream outputStream =
			new FileOutputStream (
				filename);

		@Cleanup
		Writer javaWriter =
			new OutputStreamWriter (
				outputStream);

		javaWriter.write (
			stringFormat (
				"package %s.model;\n\n",
				plugin.packageName ()));

		writeStandardImports (
			javaWriter);

		writeClassAnnotations (
			javaWriter);

		writeClass (
			javaWriter,
			modelMeta,
			className);

	}

	private
	void writeStandardImports (
			Writer javaWriter)
		throws IOException {

		List<Class<?>> standardImportClasses =
			ImmutableList.<Class<?>>of (

			java.util.LinkedHashMap.class,
			java.util.LinkedHashSet.class,
			java.util.Map.class,
			java.util.Set.class,

			lombok.Data.class,
			lombok.EqualsAndHashCode.class,
			lombok.ToString.class,
			lombok.experimental.Accessors.class,

			org.apache.commons.lang3.builder.CompareToBuilder.class,

			wbs.framework.entity.annotations.CodeField.class,
			wbs.framework.entity.annotations.CollectionField.class,
			wbs.framework.entity.annotations.DeletedField.class,
			wbs.framework.entity.annotations.DescriptionField.class,
			wbs.framework.entity.annotations.GeneratedIdField.class,
			wbs.framework.entity.annotations.LinkField.class,
			wbs.framework.entity.annotations.MajorEntity.class,
			wbs.framework.entity.annotations.ParentField.class,
			wbs.framework.entity.annotations.SimpleField.class,
			wbs.framework.entity.annotations.SlaveField.class,
			wbs.framework.record.MajorRecord.class,
			wbs.framework.record.Record.class

		);

		for (
			Class<?> standardImportClass
				: standardImportClasses
		) {

			javaWriter.write (
				stringFormat (
					"import %s;\n",
					standardImportClass.getName ()));

		}

		javaWriter.write (
			stringFormat (
				"\n"));

	}

	private
	void writeClassAnnotations (
			Writer javaWriter)
		throws IOException {

		javaWriter.write (
			stringFormat (
				"@Accessors (chain = true)\n"));

		javaWriter.write (
			stringFormat (
				"@Data\n"));

		javaWriter.write (
			stringFormat (
				"@EqualsAndHashCode (of = \"id\")\n"));

		javaWriter.write (
			stringFormat (
				"@ToString (of = \"id\")\n"));

		javaWriter.write (
			stringFormat (
				"@MajorEntity\n"));

	}

	void writeClass (
			Writer javaWriter,
			ModelMetaSpec modelMeta,
			String className)
		throws IOException {

		javaWriter.write (
			stringFormat (

				"public\n",

				"class %s\n",
				className,

				"\timplements MajorRecord<%s> {\n",
				className,

				"\n"));

		generateFields (
			javaWriter,
			modelMeta);

		generateCollections (
			javaWriter,
			modelMeta);

		generateCompareTo (
			javaWriter,
			modelMeta,
			className);

		javaWriter.write (
			stringFormat (
				"}\n"));

	}

	private
	void generateFields (
			Writer javaWriter,
			ModelMetaSpec modelMeta)
		throws IOException {

		javaWriter.write (
			stringFormat (

				"\t// fields\n",

				"\n"));

		modelWriter.descend (
			modelMeta,
			modelMeta.fields (),
			javaWriter);

	}

	private
	void generateCollections (
			Writer javaWriter,
			ModelMetaSpec modelMeta)
		throws IOException {

		javaWriter.write (
			stringFormat (

				"\t// collections\n",

				"\n"));

		modelWriter.descend (
			modelMeta,
			modelMeta.collections (),
			javaWriter);

	}

	private
	void generateCompareTo (
			Writer javaWriter,
			ModelMetaSpec modelMeta,
			String className)
		throws IOException {

		javaWriter.write (
			stringFormat (

				"\t// compare to\n",

				"\n"));

		javaWriter.write (
			stringFormat (

				"\t@Override\n",

				"\tpublic\n",

				"\tint compareTo (\n",

				"\t\t\tRecord<%s> otherRecord) {\n",
				className,

				"\n"));

		javaWriter.write (
			stringFormat (

				"\t\t%s other =\n",
				className,

				"\t\t\t(%s) otherRecord;\n",
				className,

				"\n"));

		javaWriter.write (
			stringFormat (

				"\t\treturn new CompareToBuilder ()\n",

				"\n"));

		ParentFieldSpec parentField = null;
		CodeFieldSpec codeField = null;

		for (
			ModelFieldSpec modelField
				: modelMeta.fields ()
		) {

			if (modelField instanceof ParentFieldSpec) {

				parentField =
					(ParentFieldSpec)
					modelField;

			}

			if (modelField instanceof CodeFieldSpec) {

				codeField =
					(CodeFieldSpec)
					modelField;

			}

		}

		if (parentField != null) {

			javaWriter.write (
				stringFormat (

					"\t\t\t.append (\n",

					"\t\t\t\tget%s (),\n",
					capitalise (parentField.typeName ()),

					"\t\t\t\tother.get%s ())\n",
					capitalise (parentField.typeName ()),

					"\n"));

		}

		if (codeField != null) {

			javaWriter.write (
				stringFormat (

					"\t\t\t.append (\n",

					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							codeField.name (),
							"code")),

					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							codeField.name (),
							"code")),

					"\n"));

		}

		javaWriter.write (
			stringFormat (

				"\t\t\t.toComparison ();\n",

				"\n"));

		javaWriter.write (
			stringFormat (

				"\t}\n",

				"\n"));

	}

}
