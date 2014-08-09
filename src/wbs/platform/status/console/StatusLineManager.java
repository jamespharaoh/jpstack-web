package wbs.platform.status.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;

import com.google.common.collect.ImmutableList;

@Log4j
@SingletonComponent ("statusLineManager")
public
class StatusLineManager {

	@Inject
	Map<String,StatusLine> statusLinesByBeanName =
		Collections.emptyMap ();

	@Getter
	Collection<StatusLine> statusLines;

	@PostConstruct
	public
	void afterPropertiesSet ()
		throws Exception {

		Set<String> statusLineNames =
			new HashSet<String> ();

		ImmutableList.Builder<StatusLine> statusLinesBuilder =
			ImmutableList.<StatusLine>builder ();

		log.debug (
			"About to initialise status lines");

		for (Map.Entry<String,StatusLine> entry
				: statusLinesByBeanName.entrySet ()) {

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
