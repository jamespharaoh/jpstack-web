package wbs.platform.api.module;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.WebFile;
import wbs.platform.api.resource.ApiResource;
import wbs.platform.api.resource.ApiResource.Method;

@Accessors (fluent = true)
@DataClass ("api-module")
@PrototypeComponent ("apiModuleImpl")
public
class ApiModuleImplementation
	implements ApiModule {

	// dependencies

	@Inject
	Provider<ApiResource> apiResourceProvider;

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

	// state

	Set<String> resourceNames =
		new HashSet<String> ();

	Map<Pair<String,Method>,RequestHandler> requestHandlers =
		new HashMap<Pair<String,Method>,RequestHandler> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		List<String> resourceNamesList =
			new ArrayList<String> (
				resourceNames);

		Collections.sort (
			resourceNamesList);

		for (
			String resourceName :
				resourceNamesList
		) {

			System.out.println ("RESOURCE " + resourceName);

			ApiResource resource =
				apiResourceProvider.get ();

			for (
				Method method
					: Method.values ()
			) {

				Pair<String,Method> key =
					Pair.of (
						resourceName,
						method);

				RequestHandler requestHandler =
					requestHandlers.get (
						key);

				if (requestHandler == null)
					continue;

				resource.requestHandlers ().put (
					method,
					requestHandler);

			}

			files.put (
				resourceName,
				resource);

		}

	}

	// implementation

	public
	void addRequestHandler (
			String resourceName,
			Method method,
			RequestHandler requestHandler) {

		Pair<String,Method> key =
			Pair.of (
				resourceName,
				method);

		if (requestHandlers.containsKey (key)) {

			throw new RuntimeException (
				stringFormat (
					"Method %s ",
					method,
					"implemented twice for resource %s",
					resourceName));

		}

		resourceNames.add (
			resourceName);

		requestHandlers.put (
			key,
			requestHandler);

	}

}
