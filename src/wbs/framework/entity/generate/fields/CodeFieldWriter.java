package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.CodeFieldSpec;

@PrototypeComponent ("codeFieldWriter")
@ModelWriter
public
class CodeFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	CodeFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeClass (
				String.class)

			.propertyName (
				ifNull (
					spec.name (),
					"code"))

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}
