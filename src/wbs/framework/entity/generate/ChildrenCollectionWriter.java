package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ChildrenCollectionSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("childrenCollectionWriter")
@ModelWriter
public
class ChildrenCollectionWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ChildrenCollectionSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		javaWriter.write (

			"\t@CollectionField\n",

			"\tSet<%s.model.%sRec> %ss =\n",
			fieldTypePlugin.packageName (),
			capitalise (spec.typeName ()),
			spec.typeName (),

			"\t\tnew LinkedHashSet<%s.model.%sRec> ();\n",
			fieldTypePlugin.packageName (),
			capitalise (spec.typeName ()),

			"\n");

	}

}
