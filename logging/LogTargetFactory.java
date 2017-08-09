package wbs.framework.logging;

public
interface LogTargetFactory {

	LogTarget createLogTarget (
			CharSequence staticContext);

}
