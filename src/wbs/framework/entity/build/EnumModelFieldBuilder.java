package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.classForNameRequired;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

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
import wbs.framework.entity.meta.EnumFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

@PrototypeComponent ("enumModelFieldBuilder")
@ModelBuilder
public
class EnumModelFieldBuilder {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	EnumFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

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

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				fieldName)

			.label (
				camelToSpaces (
					fieldName))

			.type (
				ModelFieldType.simple)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				classForNameRequired (
					fullFieldTypeName))

			.nullable (
				ifNull (
					spec.nullable (),
					false))

			.columnNames (
				ImmutableList.<String>of (
					ifNull (
						spec.columnName (),
						camelToUnderscore (
							fieldName))));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

	}

}
