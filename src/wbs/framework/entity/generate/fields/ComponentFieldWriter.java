package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.ComponentFieldSpec;

@PrototypeComponent ("componentFieldWriter")
@ModelWriter
public
class ComponentFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ComponentFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				capitalise (
					spec.typeName ()))

			.propertyName (
				ifNull (
					spec.name (),
					spec.typeName ()))

			.defaultValue (
				imports ->
					stringFormat (
						"new %s ()",
						imports.registerFormat (
							"%s.model.%s",
							context.modelMeta ().plugin ().packageName (),
							capitalise (
								spec.typeName ()))))

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
