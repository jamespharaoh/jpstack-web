package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.MasterFieldSpec;

@PrototypeComponent ("masterFieldWriter")
@ModelWriter
public
class MasterFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	MasterFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()));

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"%s",
				fullFieldTypeName)

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (),
					spec.typeName ()))

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
