package wbs.framework.application.context;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.reflect.MethodUtils;

@Accessors (fluent = true)
public
class MethodBeanFactory
	implements BeanFactory {

	@Getter @Setter
	Object factoryBean;

	@Getter @Setter
	String factoryMethodName;

	@Override
	@SneakyThrows (Exception.class)
	public
	Object instantiate () {

		return MethodUtils.invokeMethod (
			factoryBean,
			factoryMethodName);

	}

}
