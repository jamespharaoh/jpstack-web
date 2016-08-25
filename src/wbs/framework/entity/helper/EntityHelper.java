package wbs.framework.entity.helper;

import java.util.List;
import java.util.Map;

import wbs.framework.entity.model.Model;

public
interface EntityHelper {

	List <String> entityClassNames ();
	List <Class <?>> entityClasses ();

	List <Model> models ();
	Map <Class <?>, Model> modelsByClass ();
	Map <String, Model> modelsByName ();

}
