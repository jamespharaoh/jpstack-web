package wbs.framework.application.context;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class SingletonBeanFactory
	implements BeanFactory {

	@Getter @Setter
	Object object;

	@Override
	public
	Object instantiate () {
		return object;
	}

}
