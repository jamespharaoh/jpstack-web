package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.ChildrenMappingSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("childrenMappingWriter")
@ModelWriter
public
class ChildrenMappingWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	@Inject
	ModelMetaLoader modelMetaLoader;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fieldName =
			ifNull (
				spec.name (),
				stringFormat (
					"%ss",
					spec.typeName ()));

		String fieldTypeName =
			stringFormat (
				"%sRec",
				capitalise (
					spec.typeName ()));

		// write field annotation

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				"CollectionField");

		if (spec.joinColumnName () != null) {

			annotationWriter.addAttributeFormat (
				"key",
				"\"%s\"",
				spec.joinColumnName ().replace ("\"", "\\\""));

		}

		annotationWriter.addAttributeFormat (
			"index",
			"\"%s\"",
			spec.mapColumnName ().replace ("\"", "\\\""));

		annotationWriter.write (
			javaWriter,
			"\t");

		// write field

		if (
			equal (
				spec.mapType (),
				"integer")
		) {

			javaWriter.write (
				"\tMap<Integer,%s.model.%s> %s =\n",
				fieldTypePlugin.packageName (),
				fieldTypeName,
				fieldName);

			javaWriter.write (
				"\t\tnew LinkedHashMap<Integer,%s.model.%s> ();\n",
				fieldTypePlugin.packageName (),
				fieldTypeName);

		} else if (
			equal (
				spec.mapType (),
				"string")
		) {

			javaWriter.write (
				"\tMap<String,%s.model.%s> %s =\n",
				fieldTypePlugin.packageName (),
				fieldTypeName,
				fieldName);

			javaWriter.write (
				"\t\tnew LinkedHashMap<String,%s.model.%s> ();\n",
				fieldTypePlugin.packageName (),
				fieldTypeName);

		} else {

			throw new RuntimeException ();

		}

		javaWriter.write (
			"\n");

	}

}
