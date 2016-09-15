package wbs.framework.entity.generate.fields;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.TypeFieldSpec;

@PrototypeComponent ("typeFieldWriter")
@ModelWriter
public
class TypeFieldWriter {

	// singleton dependencies

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	TypeFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		String fieldTypeName =
			ifNull (
				spec.typeName (),
				stringFormat (
					"%sType",
					context.modelMeta ().name ()));

		String fieldName =
			ifNull (
				spec.name (),
				fieldTypeName);

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				fieldTypeName);

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					fieldTypeName));

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeName (
				fullFieldTypeName)

			.propertyName (
				fieldName)

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
