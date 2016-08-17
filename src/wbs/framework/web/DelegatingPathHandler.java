package wbs.framework.web;

import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;

/**
 * Implementation of PathHandler which delegates to other PathHandlers or
 * WebFiles based on simple string mappings.
 */
@Log4j
@PrototypeComponent ("delegatingPathHandler")
public
class DelegatingPathHandler
	implements PathHandler {

	@Getter @Setter
	Map <String, PathHandler> paths =
		new HashMap<> ();

	@Getter @Setter
	Map <String, WebFile> files =
		new HashMap<> ();

	@Inject
	Map <String, ServletModule> servletModules;

	/**
	 * Populates paths and files properties with values obtained from any
	 * ServletModules in the application context.
	 */
	@PostConstruct
	public
	void afterPropertiesSet () {

System.out.println ("");
System.out.println ("");
System.out.println ("#################################################");
System.out.println ("#################################################");
System.out.println ("############                      ###############");
System.out.println ("############ AFTER PROPERTIES SET ###############");
System.out.println ("############                      ###############");
System.out.println ("#################################################");
System.out.println ("#################################################");
System.out.println ("");
System.out.println ("");

		Map <String, String> pathDeclaredByModule =
			new HashMap<> ();

		Map <String, String> fileDeclaredByModule =
			new HashMap<> ();

		// for each one...

		for (
			Map.Entry <String, ServletModule> servletModuleEntry
				: servletModules.entrySet ()
		) {

			String servletModuleName =
				servletModuleEntry.getKey ();

			ServletModule servletModule =
				servletModuleEntry.getValue ();

			// import all its paths

			Map <String, ? extends PathHandler> modulePaths =
				servletModule.paths ();

			if (modulePaths != null) {

				for (
					Map.Entry <String, ? extends PathHandler> modulePathEntry
						: modulePaths.entrySet ()
				) {

					String modulePathName =
						modulePathEntry.getKey ();

					PathHandler modulePathHandler =
						modulePathEntry.getValue ();

					if (
						pathDeclaredByModule.containsKey (
							modulePathName)
					) {

						throw new RuntimeException (
							stringFormat (
							"Duplicated path '%s' (in %s and %s)",
							modulePathName,
							pathDeclaredByModule.get (
								modulePathName),
							servletModuleName));

					}

					pathDeclaredByModule.put (
						modulePathName,
						servletModuleName);

					log.debug (
						"Adding path " + modulePathName);

					paths.put (
						modulePathName,
						modulePathHandler);

				}

			}

			// import all its files

			Map <String, ? extends WebFile> moduleFiles =
				servletModule.files ();

			if (moduleFiles != null) {

				for (
					Map.Entry <String, ? extends WebFile> moduleFileEntry
						: moduleFiles.entrySet ()
				) {

					String moduleFileName =
						moduleFileEntry.getKey ();

					if (
						fileDeclaredByModule.containsKey (
							moduleFileName)
					) {

						throw new RuntimeException (
							stringFormat (
							"Duplicated file '%s' (in %s and %s)",
							moduleFileName,
							fileDeclaredByModule.get (
								moduleFileName),
							servletModuleName));

					}

					fileDeclaredByModule.put (
						moduleFileName,
						servletModuleName);

					log.debug (
						"Adding file " + moduleFileName);

					files.put (
						moduleFileName,
						moduleFileEntry.getValue ());

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
