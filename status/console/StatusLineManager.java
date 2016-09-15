package wbs.platform.status.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("statusLineManager")
public
class StatusLineManager {

	// singleton dependencies

	@SingletonDependency
	Map<String,StatusLine> statusLinesByBeanName =
		Collections.emptyMap ();

	// properties

	@Getter
	Collection <StatusLine> statusLines;

	// life cycle

	@NormalLifecycleSetup
	public
	void afterPropertiesSet ()
		throws Exception {

		Set <String> statusLineNames =
			new HashSet<> ();

		ImmutableList.Builder <StatusLine> statusLinesBuilder =
			ImmutableList.builder ();

		log.debug (
			"About to initialise status lines");

		for (
			Map.Entry <String, StatusLine> entry
				: statusLinesByBeanName.entrySet ()
		) {

			String beanName =
				entry.getKey ();

			StatusLine statusLine =
				entry.getValue ();

			String statusLineName =
				statusLine.getName ();

			log.debug (
				stringFormat (
					"Adding status line %s from %s",
					statusLineName,
					beanName));

			if (
				statusLineNames
					.contains (statusLineName)
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated status line name %s in %s",
						statusLineName,
						beanName));

			}

			statusLineNames.add (
				statusLineName);

			statusLinesBuilder.add (
				statusLine);

			log.debug (
				stringFormat (
					"Adding status line %s from %s",
					statusLineName,
					beanName));

		}

		statusLines =
			statusLinesBuilder.build ();

		log.info (
			stringFormat (
				"Initialised %s status lines",
				statusLines.size ()));

	}

}
