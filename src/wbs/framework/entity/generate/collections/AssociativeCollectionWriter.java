package wbs.framework.entity.generate.collections;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.collections.AssociativeCollectionSpec;

@PrototypeComponent ("associativeCollectionWriter")
@ModelWriter
public
class AssociativeCollectionWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	AssociativeCollectionSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		String fullFieldTypeName;

		if (
			stringEqualSafe (
				spec.typeName (),
				"string")
		) {

			fullFieldTypeName =
				"java.lang.String";

		} else {

			PluginModelSpec fieldTypePluginModel =
				pluginManager.pluginModelsByName ().get (
					spec.typeName ());

			PluginSpec fieldTypePlugin =
				fieldTypePluginModel.plugin ();

			fullFieldTypeName =
				stringFormat (
					"%s.model.%sRec",
					fieldTypePlugin.packageName (),
					capitalise (
						spec.typeName ()));

		}

		String fieldName =
			ifNull (
				spec.name (),
				naivePluralise (
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
