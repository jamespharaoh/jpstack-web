package wbs.framework.activitymanager;

import java.util.Map;

public
interface ActivityManager {

	ActiveTask start (
			String taskName,
			Map<String,Object> parameters);

}
