package wbs.framework.component.tools;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class SingletonComponentFactory
	implements ComponentFactory {

	@Getter @Setter
	Object object;

	@Override
	public
	Object makeComponent () {
		return object;
	}

}
