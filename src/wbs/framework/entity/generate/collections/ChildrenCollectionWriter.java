package wbs.framework.entity.generate.collections;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.LinkedHashSet;
import java.util.Set;

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
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.collections.ChildrenCollectionSpec;

@PrototypeComponent ("childrenCollectionWriter")
@ModelWriter
public
class ChildrenCollectionWriter {

	// singleton dependencies

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ChildrenCollectionSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		String fieldName =
			ifNull (
				spec.name (),
				naivePluralise (
					spec.typeName ()));

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		if (
			isNull (
				fieldTypePluginModel)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field '%s.%s' ",
					context.modelMeta ().name (),
					fieldName,
					"has type '%s' ",
					spec.typeName (),
					"which does not exist"));


		}

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
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeName (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							Set.class),
						imports.register (
							fullFieldTypeName)))

			.propertyName (
				fieldName)

			.defaultValue (
				imports ->
					stringFormat (
						"new %s <%s> ()",
						imports.register (
							LinkedHashSet.class),
						imports.register (
							fullFieldTypeName)))

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
