package wbs.framework.schema.helper;

import java.util.List;
import java.util.Map;

public interface SchemaTypesHelper {

	Map<Class<?>,List<String>> fieldTypeNames ();
	Map<String,List<String>> enumTypes ();

}
