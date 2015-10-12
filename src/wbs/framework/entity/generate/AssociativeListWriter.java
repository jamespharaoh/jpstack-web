package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
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
import wbs.framework.entity.meta.AssociativeListSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("associativeListWriter")
@ModelWriter
public
class AssociativeListWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	AssociativeListSpec spec;

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
				"LinkField");

		annotationWriter.addAttributeFormat (
			"table",
			"\"%s\"",
			spec.tableName ().replace ("\"", "\\\""));

		annotationWriter.addAttributeFormat (
			"index",
			"\"%s\"",
			"index");

		annotationWriter.write (
			javaWriter,
			"\t");

		// write field

		javaWriter.write (
			"\tList<%s.model.%s> %s =\n",
			fieldTypePlugin.packageName (),
			fieldTypeName,
			fieldName);

		javaWriter.write (
			"\t\tnew ArrayList<%s.model.%s> ();\n",
			fieldTypePlugin.packageName (),
			fieldTypeName);

		javaWriter.write (
			"\n");

	}

}
