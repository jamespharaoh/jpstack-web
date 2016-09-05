package wbs.framework.activitymanager;

import java.util.Map;

public
interface ActivityManager {

	ActiveTask start (
			String taskType,
			String summary,
			Object owner,
			Map<String,String> parameters);

	ActiveTask start (
			String taskType,
			String summary,
			Object owner);

	Task currentTask ();

}
