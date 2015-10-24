package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.CodeFieldSpec;
import wbs.framework.entity.meta.DateFieldSpec;
import wbs.framework.entity.meta.IdentityIntegerFieldSpec;
import wbs.framework.entity.meta.IdentityReferenceFieldSpec;
import wbs.framework.entity.meta.IdentityStringFieldSpec;
import wbs.framework.entity.meta.IndexFieldSpec;
import wbs.framework.entity.meta.IntegerFieldSpec;
import wbs.framework.entity.meta.MasterFieldSpec;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelImplementsInterfaceSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ModelMetaType;
import wbs.framework.entity.meta.ParentFieldSpec;
import wbs.framework.entity.meta.ParentIdFieldSpec;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.entity.meta.ReferenceFieldSpec;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.entity.meta.TypeCodeFieldSpec;
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
	ModelMetaSpec modelMeta;

	// state

	String recordClassName;

	// implementation

	public
	void generateRecord ()
		throws IOException {

		if (modelMeta.type ().record ()) {

			recordClassName =
				stringFormat (
					"%sRec",
					capitalise (
						modelMeta.name ()));

		} else if (modelMeta.type ().component ()) {

			recordClassName =
				capitalise (
					modelMeta.name ());

		} else {

			throw new RuntimeException ();

		}

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
				recordClassName);

		@Cleanup
		FormatWriter javaWriter =
			new AtomicFileWriter (
				filename);

		javaWriter.writeFormat (
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

			org.apache.commons.lang3.builder.CompareToBuilder.class,

			wbs.framework.entity.annotations.CommonEntity.class,
			wbs.framework.entity.annotations.ComponentEntity.class,
			wbs.framework.entity.annotations.EphemeralEntity.class,
			wbs.framework.entity.annotations.EventEntity.class,
			wbs.framework.entity.annotations.MajorEntity.class,
			wbs.framework.entity.annotations.MinorEntity.class,
			wbs.framework.entity.annotations.RootEntity.class,
			wbs.framework.entity.annotations.TypeEntity.class,

			wbs.framework.entity.annotations.AssignedIdField.class,
			wbs.framework.entity.annotations.CodeField.class,
			wbs.framework.entity.annotations.CollectionField.class,
			wbs.framework.entity.annotations.ComponentField.class,
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
			wbs.framework.entity.annotations.TypeCodeField.class,
			wbs.framework.entity.annotations.TypeField.class,

			wbs.framework.object.ObjectTypeEntry.class,

			wbs.framework.record.CommonRecord.class,
			wbs.framework.record.EphemeralRecord.class,
			wbs.framework.record.EventRecord.class,
			wbs.framework.record.MajorRecord.class,
			wbs.framework.record.MinorRecord.class,
			wbs.framework.record.Record.class,
			wbs.framework.record.RecordComponent.class,
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

			javaWriter.writeFormat (
				"import %s;\n",
				standardImportClass.getName ());

		}

		javaWriter.writeFormat (
			"\n");

	}

	private
	void writeClassAnnotations (
			FormatWriter javaWriter)
		throws IOException {

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				stringFormat (
					"%sEntity",
					capitalise (
						modelMeta.type ().toString ())));

		if (isNotNull (modelMeta.tableName ())) {

			annotationWriter.addAttributeFormat (
				"table",
				"\"%s\"",
				modelMeta.tableName ().replace ("\"", "\\\""));

		}

		if (! ifNull (modelMeta.create (), true)) {

			annotationWriter.addAttributeFormat (
				"create",
				"false");

		}

		if (! ifNull (modelMeta.mutable (), true)) {

			annotationWriter.addAttributeFormat (
				"mutable",
				"false");

		}

		annotationWriter.write (
			javaWriter,
			"");

	}

	void writeClass (
			FormatWriter javaWriter) {

		javaWriter.writeFormat (
			"public\n");

		javaWriter.writeFormat (
			"class %s\n",
			recordClassName);

		switch (modelMeta.type ()) {

		case common:

			javaWriter.writeFormat (
				"\timplements CommonRecord<%s>",
				recordClassName);

			break;

		case component:

			javaWriter.writeFormat (
				"\timplements RecordComponent<%s>",
				recordClassName);

			break;

		case ephemeral:

			javaWriter.writeFormat (
				"\timplements EphemeralRecord<%s>",
				recordClassName);

			break;

		case event:

			javaWriter.writeFormat (
				"\timplements EventRecord<%s>",
				recordClassName);

			break;

		case major:

			javaWriter.writeFormat (
				"\timplements MajorRecord<%s>",
				recordClassName);

			break;

		case minor:

			javaWriter.writeFormat (
				"\timplements MinorRecord<%s>",
				recordClassName);

			break;

		case root:

			javaWriter.writeFormat (
				"\timplements RootRecord<%s>",
				recordClassName);

			break;

		case type:

			javaWriter.writeFormat (
				"\timplements TypeRecord<%s>",
				recordClassName);

			break;

		default:

			throw new RuntimeException ();

		}

		for (
			ModelImplementsInterfaceSpec implementsInterface
				: modelMeta.implementsInterfaces ()
		) {

			javaWriter.writeFormat (
				", %s.%s",
				implementsInterface.packageName (),
				implementsInterface.name ());

		}

		javaWriter.writeFormat (
			" {\n");

		javaWriter.writeFormat (
			"\n");

		generateFields (
			javaWriter);

		generateCollections (
			javaWriter);

		generateEquals (
			javaWriter);

		if (modelMeta.type ().record ()) {

			generateCompareTo (
				javaWriter);

			generateToString (
				javaWriter);

		}

		javaWriter.writeFormat (
			"}\n");

	}

	private
	void generateFields (
			FormatWriter javaWriter) {

		if (modelMeta.fields ().isEmpty ()) {
			return;
		}

		javaWriter.writeFormat (
			"\t// fields\n");

		javaWriter.writeFormat (
			"\n");

		ModelFieldWriterContext nextContext =
			new ModelFieldWriterContext ()

			.modelMeta (
				modelMeta)

			.recordClassName (
				recordClassName);

		modelWriterBuilder.write (
			nextContext,
			modelMeta.fields (),
			javaWriter);

	}

	private
	void generateCollections (
			FormatWriter javaWriter) {

		if (modelMeta.collections ().isEmpty ()) {
			return;
		}

		javaWriter.writeFormat (
			"\t// collections\n");

		javaWriter.writeFormat (
			"\n");

		ModelFieldWriterContext nextContext =
			new ModelFieldWriterContext ()

			.modelMeta (
				modelMeta)

			.recordClassName (
				recordClassName);

		modelWriterBuilder.write (
			nextContext,
			modelMeta.collections (),
			javaWriter);

	}

	private
	void generateEquals (
			FormatWriter javaWriter) {

		// write comment

		javaWriter.writeFormat (
			"\t// equals\n");

		javaWriter.writeFormat (
			"\n");

		// write override annotation

		javaWriter.writeFormat (
			"\t@Override\n");

		// write function definition

		javaWriter.writeFormat (
			"\tpublic\n");

		javaWriter.writeFormat (
			"\tboolean equals (\n");

		javaWriter.writeFormat (
			"\t\t\tObject otherObject) {\n");

		javaWriter.writeFormat (
			"\n");

		// write class comparison

		javaWriter.writeFormat (
			"\t\tif (otherObject.getClass () != %s.class) {\n",
			recordClassName);

		javaWriter.writeFormat (
			"\t\t\treturn false;\n");

		javaWriter.writeFormat (
			"\t\t}\n");

		javaWriter.writeFormat (
			"\n");

		// write cast

		javaWriter.writeFormat (
			"\t\t%s other =\n",
			recordClassName);

		javaWriter.writeFormat (
			"\t\t\t(%s)\n",
			recordClassName);

		javaWriter.writeFormat (
			"\t\t\totherObject;\n");

		javaWriter.writeFormat (
			"\n");

		if (modelMeta.type ().record ()) {

			// check for null id

			javaWriter.writeFormat (
				"\t\tif (getId () == null || other.getId () == null) {\n");

			javaWriter.writeFormat (
				"\t\t\treturn false;\n");

			javaWriter.writeFormat (
				"\t\t}\n");

			javaWriter.writeFormat (
				"\n");

			// compare id

			javaWriter.writeFormat (
				"\t\treturn getId () == other.getId ();\n");

			javaWriter.writeFormat (
				"\n");

		} else if (modelMeta.type ().component ()) {

			// TODO this is not nice at all

			for (
				ModelFieldSpec modelField
					: modelMeta.fields ()
			) {

				String fieldName;

				if (modelField instanceof ReferenceFieldSpec) {

					ReferenceFieldSpec referenceField =
						(ReferenceFieldSpec)
						modelField;

					fieldName =
						ifNull (
							referenceField.name (),
							referenceField.typeName ());

				} else if (modelField instanceof DateFieldSpec) {

					DateFieldSpec dateField =
						(DateFieldSpec)
						modelField;

					fieldName =
						dateField.name ();

				} else if (modelField instanceof IntegerFieldSpec) {

					IntegerFieldSpec integerField =
						(IntegerFieldSpec)
						modelField;

					fieldName =
						integerField.name ();

				} else {

					throw new RuntimeException (
						stringFormat (
							"Don't know how to get name of component field ",
							"type %s",
							modelField.getClass ().getSimpleName ()));

				}

				javaWriter.writeFormat (
					"\t\tif (\n");

				javaWriter.writeFormat (
					"\t\t\t! (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s () == null\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t\t&& other.get%s () == null\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t) && (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s () == null\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t\t|| other.get%s () == null\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t\t|| ! get%s ().equals (\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t\t\tother.get%s ())\n",
					capitalise (
						fieldName));

				javaWriter.writeFormat (
					"\t\t\t)\n");

				javaWriter.writeFormat (
					"\t\t) {\n");

				javaWriter.writeFormat (
					"\t\t\treturn false;\n");

				javaWriter.writeFormat (
					"\t\t}\n");

				javaWriter.writeFormat (
					"\n");

			}

			javaWriter.writeFormat (
				"\t\treturn true;\n");

			javaWriter.writeFormat (
				"\n");

		} else {

			throw new RuntimeException ();

		}

		// write end of function

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

	private
	void generateCompareTo (
			FormatWriter javaWriter) {

		// TODO generate better code

		// write comment

		javaWriter.writeFormat (
			"\t// compare to\n");

		javaWriter.writeFormat (
			"\n");

		// write override annotation

		javaWriter.writeFormat (
			"\t@Override\n");

		// write function definition

		javaWriter.writeFormat (
			"\tpublic\n");

		javaWriter.writeFormat (
			"\tint compareTo (\n");

		javaWriter.writeFormat (
			"\t\t\tRecord<%s> otherRecord) {\n",
			recordClassName);

		javaWriter.writeFormat (
			"\n");

		// write cast to concrete type

		javaWriter.writeFormat (
			"\t\t%s other =\n",
			recordClassName);

		javaWriter.writeFormat (
			"\t\t\t(%s) otherRecord;\n",
			recordClassName);

		javaWriter.writeFormat (
			"\n");

		// create compare to builder

		javaWriter.writeFormat (
			"\t\treturn new CompareToBuilder ()\n");

		javaWriter.writeFormat (
			"\n");

		// scan fields

		ParentFieldSpec parentField = null;
		ParentTypeFieldSpec parentTypeField = null;
		ParentIdFieldSpec parentIdField = null;
		MasterFieldSpec masterField = null;

		TypeCodeFieldSpec typeCodeField = null;
		CodeFieldSpec codeField = null;
		IndexFieldSpec indexField = null;

		List<IdentityReferenceFieldSpec> identityReferenceFields =
			new ArrayList<IdentityReferenceFieldSpec> ();

		List<IdentityIntegerFieldSpec> identityIntegerFields =
			new ArrayList<IdentityIntegerFieldSpec> ();

		List<IdentityStringFieldSpec> identityStringFields =
			new ArrayList<IdentityStringFieldSpec> ();

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

			if (modelField instanceof TypeCodeFieldSpec) {

				typeCodeField =
					(TypeCodeFieldSpec)
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

				identityReferenceFields.add (
					(IdentityReferenceFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityIntegerFieldSpec) {

				identityIntegerFields.add (
					(IdentityIntegerFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityStringFieldSpec) {

				identityStringFields.add (
					(IdentityStringFieldSpec)
					modelField);

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

			javaWriter.writeFormat (
				"\t\t\t.append (\n");

			javaWriter.writeFormat (
				"\t\t\t\tother.get%s (),\n",
				capitalise (
					timestampField.name ()));

			javaWriter.writeFormat (
				"\t\t\t\tget%s ())\n",
				capitalise (
					timestampField.name ()));

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t.append (\n");

			javaWriter.writeFormat (
				"\t\t\t\tother.getId (),\n");

			javaWriter.writeFormat (
				"\t\t\t\tgetId ())\n");

			javaWriter.writeFormat (
				"\n");

		} else if (gotName || masterField != null) {

			if (parentField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				javaWriter.writeFormat (
					"\n");

			}

			if (parentTypeField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				javaWriter.writeFormat (
					"\n");

			}

			if (parentIdField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tgetParentId (),\n");

				javaWriter.writeFormat (
					"\t\t\t\tother.getParentId ())\n");

				javaWriter.writeFormat (
					"\n");

			}

			if (masterField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				javaWriter.writeFormat (
					"\n");

			}

			if (typeCodeField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				javaWriter.writeFormat (
					"\n");

			}

			if (codeField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				javaWriter.writeFormat (
					"\n");

			}

			if (indexField != null) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				javaWriter.writeFormat (
					"\n");

			}

			for (
				IdentityReferenceFieldSpec identityReferenceField
					: identityReferenceFields
			) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				javaWriter.writeFormat (
					"\n");

			}

			for (
				IdentityIntegerFieldSpec identityIntegerField
					: identityIntegerFields
			) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						identityIntegerField.name ()));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						identityIntegerField.name ()));

				javaWriter.writeFormat (
					"\n");

			}

			for (
				IdentityStringFieldSpec identityStringField
					: identityStringFields
			) {

				javaWriter.writeFormat (
					"\t\t\t.append (\n");

				javaWriter.writeFormat (
					"\t\t\t\tget%s (),\n",
					capitalise (
						identityStringField.name ()));

				javaWriter.writeFormat (
					"\t\t\t\tother.get%s ())\n",
					capitalise (
						identityStringField.name ()));

				javaWriter.writeFormat (
					"\n");

			}

		} else {

			javaWriter.writeFormat (
				"\t\t\t.append (\n");

			javaWriter.writeFormat (
				"\t\t\t\tother.getId (),\n");

			javaWriter.writeFormat (
				"\t\t\t\tgetId ())\n");

			javaWriter.writeFormat (
				"\n");

		}

		// write converstion to return value

		javaWriter.writeFormat (
			"\t\t\t.toComparison ();\n");

		javaWriter.writeFormat (
			"\n");

		// write end of function

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

	private
	void generateToString (
			FormatWriter javaWriter) {

		// write comment

		javaWriter.writeFormat (
			"\t// to string\n");

		javaWriter.writeFormat (
			"\n");

		// write function definition

		javaWriter.writeFormat (
			"\tpublic\n");

		javaWriter.writeFormat (
			"\tString toString () {\n");

		javaWriter.writeFormat (
			"\n");

		// write function body

		if (
			equal (
				modelMeta.name (),
				"text")
		) {

			javaWriter.writeFormat (
				"\t\treturn getText ();\n");

		} else {

			javaWriter.writeFormat (
				"\t\treturn \"%s(id=\" + getId () + \")\";\n",
				recordClassName);

		}

		javaWriter.writeFormat (
			"\n");

		// write end of function

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

}
