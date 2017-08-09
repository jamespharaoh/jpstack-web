package wbs.framework.logging;

import lombok.NonNull;

import org.apache.logging.log4j.LogManager;

public
class Log4jLogTargetFactory
	implements LogTargetFactory {

	@Override
	public
	LogTarget createLogTarget (
			@NonNull CharSequence staticContext) {

		return new Log4jLogTarget (
			LogManager.getLogger (
				staticContext.toString ()));

	}

}
