package wbs.framework.logging;

import lombok.NonNull;

import org.apache.log4j.Logger;

public
class Log4jLogTargetFactory
	implements LogTargetFactory {

	@Override
	public
	LogTarget createLogTarget (
			@NonNull CharSequence staticContext) {

		return new Log4jLogTarget (
			Logger.getLogger (
				staticContext.toString ()));

	}

}
