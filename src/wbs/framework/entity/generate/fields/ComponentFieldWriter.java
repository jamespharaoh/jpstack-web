package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.ComponentFieldSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

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
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"%s",
				capitalise (
					spec.typeName ()))

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (),
					spec.typeName ()))

			.defaultValueFormat (
				"new %s ()",
				capitalise (
					spec.typeName ()))

			.write (
				javaWriter,
				"\t");

	}

}
