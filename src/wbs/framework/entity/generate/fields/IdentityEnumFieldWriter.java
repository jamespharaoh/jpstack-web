package wbs.framework.entity.generate.fields;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginCustomTypeSpec;
import wbs.framework.component.scaffold.PluginEnumTypeSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelRecordGenerator;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.IdentityEnumFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("identityEnumFieldWriter")
@ModelWriter
public
class IdentityEnumFieldWriter
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	IdentityEnumFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

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

			new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeName (
					fullFieldTypeName)

				.propertyName (
					fieldName)

				.setUpdatedFieldName (
					ModelRecordGenerator.recordUpdatedFieldName)

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ())

			;

		}

	}

}
