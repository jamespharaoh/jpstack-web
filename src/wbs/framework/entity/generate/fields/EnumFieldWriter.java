package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginCustomTypeSpec;
import wbs.framework.application.scaffold.PluginEnumTypeSpec;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.EnumFieldSpec;

@PrototypeComponent ("enumFieldWriter")
@ModelWriter
public
class EnumFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	EnumFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		String fieldTypePackageName;

		PluginEnumTypeSpec fieldTypePluginEnumType =
			pluginManager.pluginEnumTypesByName ().get (
				spec.typeName ());

		PluginCustomTypeSpec fieldTypePluginCustomType =
			pluginManager.pluginCustomTypesByName ().get (
				spec.typeName ());

		if (fieldTypePluginEnumType != null) {

			PluginSpec fieldTypePlugin =
				fieldTypePluginEnumType.plugin ();

			fieldTypePackageName =
				fieldTypePlugin.packageName ();

		} else if (fieldTypePluginCustomType != null) {

			PluginSpec fieldTypePlugin =
				fieldTypePluginCustomType.plugin ();

			fieldTypePackageName =
				fieldTypePlugin.packageName ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"No such enum or custom type: %s",
					spec.typeName ()));

		}

		String fieldName =
			ifNull (
				spec.name (),
				spec.typeName ());

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%s",
				fieldTypePackageName,
				capitalise (
					spec.typeName ()));

		// write field

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeName (
				fullFieldTypeName)

			.propertyName (
				fieldName);

		if (spec.defaultValue () != null) {

			propertyWriter.defaultValueFormat (
				"%s.%s",
				fullFieldTypeName,
				spec.defaultValue ());

		}

		propertyWriter.writeBlock (
			target.imports (),
			target.formatWriter ());

	}

}
