package wbs.platform.api.module;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.PathHandler;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@DataClass ("api-module")
@PrototypeComponent ("apiModuleImpl")
public
class ApiModuleImplementation
	implements ApiModule {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// properties

	@DataChildren
	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@DataChildren
	@Getter @Setter
	Map<String,PathHandler> paths =
		new LinkedHashMap<String,PathHandler> ();

}
