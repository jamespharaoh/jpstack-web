package wbs.framework.application.context;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class SingletonBeanFactory
	implements InitializedComponentFactory {

	@Getter @Setter
	Object object;

	@Override
	public
	Object makeComponent () {
		return object;
	}

}
