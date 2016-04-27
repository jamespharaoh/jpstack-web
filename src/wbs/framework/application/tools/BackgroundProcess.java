package wbs.framework.application.tools;

public
interface BackgroundProcess {

	BackgroundProcess runAutomatically (
			Boolean runAutomatically);

	void runNow ();

}
