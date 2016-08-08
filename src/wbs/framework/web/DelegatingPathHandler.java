package wbs.framework.web;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * Implementation of PathHandler which delegates to other PathHandlers or
 * WebFiles based on simple string mappings.
 */
@Log4j
public
class DelegatingPathHandler
	implements PathHandler {

	@Getter @Setter
	Map<String,PathHandler> paths =
		new HashMap<String,PathHandler> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new HashMap<String,WebFile> ();

	@Inject
	Map<String,ServletModule> servletModules;

	/**
	 * Populates paths and files properties with values obtained from any
	 * ServletModules in the application context.
	 */
	@PostConstruct
	public
	void afterPropertiesSet () {

		// for each one...

		for (ServletModule servletModule
				: servletModules.values ()) {

			// import all its paths

			Map<String,? extends PathHandler> modulePaths =
				servletModule.paths ();

			if (modulePaths != null) {

				for (Map.Entry<String,? extends PathHandler> ent
						: modulePaths.entrySet ()) {

					if (paths.containsKey (
							ent.getKey ())) {

						throw new RuntimeException (
							"Duplicated path: " +
							ent.getKey ());

					}

					log.debug (
						"Adding path " + ent.getKey ());

					paths.put (
						ent.getKey (),
						ent.getValue ());

				}

			}

			// import all its files

			Map<String,? extends WebFile> moduleFiles =
				servletModule.files ();

			if (moduleFiles != null) {

				for (Map.Entry<String,? extends WebFile> ent
						: moduleFiles.entrySet ()) {

					if (files.containsKey (
							ent.getKey ())) {

						throw new RuntimeException (
							"Duplicated file: " +
							ent.getKey ());

					}

					log.debug (
						"Adding file " + ent.getKey ());

					files.put (
						ent.getKey (),
						ent.getValue ());

				}

			}

		}

	}

	@Override
	public
	WebFile processPath (
			String path)
		throws ServletException {

		log.debug (
			stringFormat (
				"processPath \"%s\"",
				path));

		// strip any trailing '/'

		if (path.endsWith ("/")) {

			path =
				path.substring (
					0,
					path.length () - 1);

		}

		// check for a file with the exact path

		if (files != null) {

			WebFile webFile =
				files.get (path);

			if (webFile != null)
				return webFile;

		}

		// ok, look for a handler, and keep stripping off bits until we find one

		if (paths != null) {

			String remain = "";

			while (true) {

				PathHandler pathHandler =
					paths.get (path);

				if (pathHandler != null) {

					return pathHandler.processPath (
						remain);

				}

				int slashPosition =
					path.lastIndexOf ('/');

				if (slashPosition == 0)
					return null;

				if (slashPosition == -1)
					return null;

				remain =
					joinWithoutSeparator (
						path.substring (
							slashPosition),
						remain);

				path =
					path.substring (
						0,
						slashPosition);

			}

		}

		return null;

	}

}
