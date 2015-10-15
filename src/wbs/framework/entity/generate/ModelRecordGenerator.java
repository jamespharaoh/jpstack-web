package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
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
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.CodeFieldSpec;
import wbs.framework.entity.meta.IdentityIntegerFieldSpec;
import wbs.framework.entity.meta.IdentityReferenceFieldSpec;
import wbs.framework.entity.meta.IndexFieldSpec;
import wbs.framework.entity.meta.MasterFieldSpec;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ModelMetaType;
import wbs.framework.entity.meta.ParentFieldSpec;
import wbs.framework.entity.meta.ParentIdFieldSpec;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.entity.meta.TimestampFieldSpec;
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

		String sourceDirectory =
			stringFormat (
				"src/%s/model",
				plugin.packageName ().replace ('.', '/'));

		String sourceFilename =
			stringFormat (
				"%s/%s-model.xml",
				sourceDirectory,
				camelToHyphen (
					modelMeta.name ()));

		if (
			FileUtils.isFileNewer (
				new File (filename),
				new File (sourceFilename))
		) {
			return;
		}

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

			java.util.ArrayList.class,
			java.util.Date.class,
			java.util.LinkedHashMap.class,
			java.util.LinkedHashSet.class,
			java.util.List.class,
			java.util.Map.class,
			java.util.Set.class,

			lombok.Data.class,
			lombok.EqualsAndHashCode.class,
			lombok.ToString.class,
			lombok.experimental.Accessors.class,

			org.apache.commons.lang3.builder.CompareToBuilder.class,

			wbs.framework.entity.annotations.CommonEntity.class,
			wbs.framework.entity.annotations.EphemeralEntity.class,
			wbs.framework.entity.annotations.EventEntity.class,
			wbs.framework.entity.annotations.MajorEntity.class,
			wbs.framework.entity.annotations.MinorEntity.class,
			wbs.framework.entity.annotations.RootEntity.class,
			wbs.framework.entity.annotations.TypeEntity.class,

			wbs.framework.entity.annotations.AssignedIdField.class,
			wbs.framework.entity.annotations.CodeField.class,
			wbs.framework.entity.annotations.CollectionField.class,
			wbs.framework.entity.annotations.DeletedField.class,
			wbs.framework.entity.annotations.DescriptionField.class,
			wbs.framework.entity.annotations.ForeignIdField.class,
			wbs.framework.entity.annotations.GeneratedIdField.class,
			wbs.framework.entity.annotations.IdentityReferenceField.class,
			wbs.framework.entity.annotations.IdentitySimpleField.class,
			wbs.framework.entity.annotations.IndexField.class,
			wbs.framework.entity.annotations.LinkField.class,
			wbs.framework.entity.annotations.MasterField.class,
			wbs.framework.entity.annotations.NameField.class,
			wbs.framework.entity.annotations.ParentField.class,
			wbs.framework.entity.annotations.ParentIdField.class,
			wbs.framework.entity.annotations.ParentTypeField.class,
			wbs.framework.entity.annotations.ReferenceField.class,
			wbs.framework.entity.annotations.SimpleField.class,
			wbs.framework.entity.annotations.SlaveField.class,
			wbs.framework.entity.annotations.TypeField.class,

			wbs.framework.record.CommonRecord.class,
			wbs.framework.record.EphemeralRecord.class,
			wbs.framework.record.EventRecord.class,
			wbs.framework.record.MajorRecord.class,
			wbs.framework.record.MinorRecord.class,
			wbs.framework.record.Record.class,
			wbs.framework.record.RootRecord.class,
			wbs.framework.record.TypeRecord.class,

			org.joda.time.Instant.class,
			org.joda.time.LocalDate.class,

			org.jadira.usertype.dateandtime.joda.PersistentInstantAsString.class,
			org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp.class

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

		writeEntityAnnotation (
			javaWriter);

	}

	private
	void writeEntityAnnotation (
			FormatWriter javaWriter)
		throws IOException {

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				stringFormat (
					"%sEntity",
					capitalise (
						modelMeta.type ().toString ())));

		if (! ifNull (modelMeta.create (), true)) {

			annotationWriter.addAttributeFormat (
				"create",
				"false");

		}

		annotationWriter.write (
			javaWriter,
			"");

	}

	void writeClass (
			FormatWriter javaWriter)
		throws IOException {

		javaWriter.write (
			"public\n");

		javaWriter.write (
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

		case event:

			javaWriter.write (
				"\timplements EventRecord<%s> {\n",
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
			"\t// fields\n");

		javaWriter.write (
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
			"\t// collections\n");

		javaWriter.write (
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

		// write comment

		javaWriter.write (
			"\t// compare to\n");

		javaWriter.write (
			"\n");

		// write override annotation

		javaWriter.write (
			"\t@Override\n");

		// write function definition

		javaWriter.write (
			"\tpublic\n");

		javaWriter.write (
			"\tint compareTo (\n");

		javaWriter.write (
			"\t\t\tRecord<%s> otherRecord) {\n",
			className);

		javaWriter.write (
			"\n");

		// write cast to concrete type

		javaWriter.write (
			"\t\t%s other =\n",
			className);

		javaWriter.write (
			"\t\t\t(%s) otherRecord;\n",
			className);

		javaWriter.write (
			"\n");

		// create compare to builder

		javaWriter.write (
			"\t\treturn new CompareToBuilder ()\n");

		javaWriter.write (
			"\n");

		// scan fields

		ParentFieldSpec parentField = null;
		ParentTypeFieldSpec parentTypeField = null;
		ParentIdFieldSpec parentIdField = null;
		MasterFieldSpec masterField = null;

		CodeFieldSpec codeField = null;
		IndexFieldSpec indexField = null;
		IdentityReferenceFieldSpec identityReferenceField = null;
		IdentityIntegerFieldSpec identityIntegerField = null;

		TimestampFieldSpec timestampField = null;

		boolean gotName = false;

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

			if (modelField instanceof MasterFieldSpec) {

				masterField =
					(MasterFieldSpec)
					modelField;

			}

			if (modelField instanceof CodeFieldSpec) {

				codeField =
					(CodeFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IndexFieldSpec) {

				indexField =
					(IndexFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IdentityReferenceFieldSpec) {

				identityReferenceField =
					(IdentityReferenceFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IdentityIntegerFieldSpec) {

				identityIntegerField =
					(IdentityIntegerFieldSpec)
					modelField;

				gotName = true;

			}

			if (
				modelField instanceof TimestampFieldSpec
				&& timestampField == null
			) {

				timestampField =
					(TimestampFieldSpec)
					modelField;

			}

		}

		// write comparisons

		if (modelMeta.type () == ModelMetaType.event) {

			if (timestampField == null) {
				throw new RuntimeException ();
			}

			javaWriter.write (
				"\t\t\t.append (\n");

			javaWriter.write (
				"\t\t\t\tother.get%s (),\n",
				capitalise (
					timestampField.name ()));

			javaWriter.write (
				"\t\t\t\tget%s ())\n",
				capitalise (
					timestampField.name ()));

			javaWriter.write (
				"\n");

			javaWriter.write (
				"\t\t\t.append (\n");

			javaWriter.write (
				"\t\t\t\tother.getId (),\n");

			javaWriter.write (
				"\t\t\t\tgetId ())\n");

			javaWriter.write (
				"\n");

		} else if (gotName || masterField != null) {

			if (parentField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				javaWriter.write (
					"\n");

			}

			if (parentTypeField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				javaWriter.write (
					"\n");

			}

			if (parentIdField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tgetParentId (),\n");

				javaWriter.write (
					"\t\t\t\tother.getParentId ())\n");

				javaWriter.write (
					"\n");

			}

			if (masterField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				javaWriter.write (
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

			if (indexField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				javaWriter.write (
					"\n");

			}

			if (identityReferenceField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				javaWriter.write (
					"\n");

			}

			if (identityIntegerField != null) {

				javaWriter.write (
					"\t\t\t.append (\n");

				javaWriter.write (
					"\t\t\t\tget%s (),\n",
					capitalise (
						identityIntegerField.name ()));

				javaWriter.write (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						identityIntegerField.name ()));

				javaWriter.write (
					"\n");

			}

		} else {

			javaWriter.write (
				"\t\t\t.append (\n");

			javaWriter.write (
				"\t\t\t\tother.getId (),\n");

			javaWriter.write (
				"\t\t\t\tgetId ())\n");

			javaWriter.write (
				"\n");

		}

		// write converstion to return value

		javaWriter.write (
			"\t\t\t.toComparison ();\n");

		javaWriter.write (
			"\n");

		// write end of function

		javaWriter.write (
			"\t}\n");

		javaWriter.write (
			"\n");

	}

}
