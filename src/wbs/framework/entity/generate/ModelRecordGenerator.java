package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.entity.meta.CodeFieldSpec;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentFieldSpec;
import wbs.framework.entity.meta.ParentIdFieldSpec;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@PrototypeComponent ("modelRecordGenerator")
public
class ModelRecordGenerator {

	// dependencies

	@Inject
	ModelWriterManager modelWriterBuilder;

	// properties

	@Getter @Setter
	PluginSpec plugin;

	@Getter @Setter
	PluginModelSpec pluginModel;

	@Getter @Setter
	ModelMetaSpec modelMeta;

	// state

	String className;

	// implementation

	public
	void generateRecord ()
		throws IOException {

		className =
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
		FormatWriter javaWriter =
			new FormatWriter (
				new OutputStreamWriter (
					outputStream));

		javaWriter.write (

			"package %s.model;\n\n",
			plugin.packageName ());

		writeStandardImports (
			javaWriter);

		writeClassAnnotations (
			javaWriter);

		writeClass (
			javaWriter);

	}

	private
	void writeStandardImports (
			FormatWriter javaWriter)
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

			wbs.framework.entity.annotations.EphemeralEntity.class,
			wbs.framework.entity.annotations.MajorEntity.class,
			wbs.framework.entity.annotations.MinorEntity.class,
			wbs.framework.entity.annotations.RootEntity.class,
			wbs.framework.entity.annotations.TypeEntity.class,

			wbs.framework.entity.annotations.AssignedIdField.class,
			wbs.framework.entity.annotations.CodeField.class,
			wbs.framework.entity.annotations.CollectionField.class,
			wbs.framework.entity.annotations.DeletedField.class,
			wbs.framework.entity.annotations.DescriptionField.class,
			wbs.framework.entity.annotations.GeneratedIdField.class,
			wbs.framework.entity.annotations.LinkField.class,
			wbs.framework.entity.annotations.NameField.class,
			wbs.framework.entity.annotations.ParentField.class,
			wbs.framework.entity.annotations.ParentIdField.class,
			wbs.framework.entity.annotations.ParentTypeField.class,
			wbs.framework.entity.annotations.SimpleField.class,
			wbs.framework.entity.annotations.SlaveField.class,
			wbs.framework.entity.annotations.TypeField.class,

			wbs.framework.record.EphemeralRecord.class,
			wbs.framework.record.MajorRecord.class,
			wbs.framework.record.MinorRecord.class,
			wbs.framework.record.Record.class,
			wbs.framework.record.RootRecord.class,
			wbs.framework.record.TypeRecord.class

		);

		for (
			Class<?> standardImportClass
				: standardImportClasses
		) {

			javaWriter.write (

				"import %s;\n",
				standardImportClass.getName ());

		}

		javaWriter.write (

			"\n");

	}

	private
	void writeClassAnnotations (
			FormatWriter javaWriter)
		throws IOException {

		javaWriter.write (

			"@Accessors (chain = true)\n");

		javaWriter.write (

			"@Data\n");

		javaWriter.write (

			"@EqualsAndHashCode (of = \"id\")\n");

		javaWriter.write (

			"@ToString (of = \"id\")\n");

		switch (modelMeta.type ()) {

		case common:

			javaWriter.write (

				"@CommonEntity\n");

			break;

		case ephemeral:

			javaWriter.write (

				"@EphemeralEntity\n");

			break;

		case major:

			javaWriter.write (

				"@MajorEntity\n");

			break;

		case minor:

			javaWriter.write (

				"@MinorEntity\n");

			break;

		case root:

			javaWriter.write (

				"@RootEntity\n");

			break;

		case type:

			javaWriter.write (

				"@TypeEntity\n");

			break;

		default:

			throw new RuntimeException ();

		}

	}

	void writeClass (
			FormatWriter javaWriter)
		throws IOException {

		javaWriter.write (

			"public\n",

			"class %s\n",
			className);

		switch (modelMeta.type ()) {

		case common:

			javaWriter.write (

				"\timplements CommonRecord<%s> {\n",
				className);

			break;

		case ephemeral:

			javaWriter.write (

				"\timplements EphemeralRecord<%s> {\n",
				className);

			break;

		case major:

			javaWriter.write (

				"\timplements MajorRecord<%s> {\n",
				className);

			break;

		case minor:

			javaWriter.write (

				"\timplements MinorRecord<%s> {\n",
				className);

			break;

		case root:

			javaWriter.write (

				"\timplements RootRecord<%s> {\n",
				className);

			break;

		case type:

			javaWriter.write (

				"\timplements TypeRecord<%s> {\n",
				className);

			break;

		default:

			throw new RuntimeException ();

		}

		javaWriter.write (

			"\n");

		generateFields (
			javaWriter);

		generateCollections (
			javaWriter);

		generateCompareTo (
			javaWriter);

		javaWriter.write (

			"}\n");

	}

	private
	void generateFields (
			FormatWriter javaWriter)
		throws IOException {

		if (modelMeta.fields ().isEmpty ()) {
			return;
		}

		javaWriter.write (

			"\t// fields\n",

			"\n");

		modelWriterBuilder.write (
			modelMeta,
			modelMeta.fields (),
			javaWriter);

	}

	private
	void generateCollections (
			FormatWriter javaWriter)
		throws IOException {

		if (modelMeta.collections ().isEmpty ()) {
			return;
		}

		javaWriter.write (

			"\t// collections\n",

			"\n");

		modelWriterBuilder.write (
			modelMeta,
			modelMeta.collections (),
			javaWriter);

	}

	private
	void generateCompareTo (
			FormatWriter javaWriter)
		throws IOException {

		javaWriter.write (

			"\t// compare to\n",

			"\n");

		javaWriter.write (

			"\t@Override\n",

			"\tpublic\n",

			"\tint compareTo (\n",

			"\t\t\tRecord<%s> otherRecord) {\n",
			className,

			"\n");

		javaWriter.write (

			"\t\t%s other =\n",
			className,

			"\t\t\t(%s) otherRecord;\n",
			className,

			"\n");

		javaWriter.write (

			"\t\treturn new CompareToBuilder ()\n",

			"\n");

		ParentFieldSpec parentField = null;
		ParentTypeFieldSpec parentTypeField = null;
		ParentIdFieldSpec parentIdField = null;
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

			if (modelField instanceof ParentTypeFieldSpec) {

				parentTypeField =
					(ParentTypeFieldSpec)
					modelField;

			}

			if (modelField instanceof ParentIdFieldSpec) {

				parentIdField =
					(ParentIdFieldSpec)
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

				"\t\t\t.append (\n",

				"\t\t\t\tget%s (),\n",
				capitalise (parentField.typeName ()),

				"\t\t\t\tother.get%s ())\n",
				capitalise (parentField.typeName ()),

				"\n");

		}

		if (parentTypeField != null) {

			javaWriter.write (

				"\t\t\t.append (\n",

				"\t\t\t\tgetParentType (),\n",

				"\t\t\t\tother.getParentType ())\n",

				"\n");

		}

		if (parentIdField != null) {

			javaWriter.write (

				"\t\t\t.append (\n",

				"\t\t\t\tgetParentId (),\n",

				"\t\t\t\tother.getParentId ())\n",

				"\n");

		}

		if (codeField != null) {

			javaWriter.write (

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

				"\n");

		}

		if (parentField == null && codeField == null) {

			javaWriter.write (

				"\t\t\t.append (\n",

				"\t\t\t\tgetId (),\n",

				"\t\t\t\tother.getId ())\n",

				"\n");

		}

		javaWriter.write (

			"\t\t\t.toComparison ();\n",

			"\n");

		javaWriter.write (

			"\t}\n",

			"\n");

	}

}
