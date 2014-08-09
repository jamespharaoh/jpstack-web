package wbs.framework.logging;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;

@Accessors (fluent = true)
public
class TaskLog {

	@Getter @Setter
	Logger log;

	@Getter @Setter
	int errors;

	public
	void error (
			Object... args) {

		log.error (
			stringFormat (args));

		errors += 1;

	}

	public
	void error (
			Throwable throwable,
			Object... args) {

		log.error (
			stringFormat (args),
			throwable);

		errors += 1;

	}

}
