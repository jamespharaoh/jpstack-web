package wbs.platform.hooks.logic;

import lombok.Getter;

public abstract
class HooksProxyImpl<
		Parent,
		Proxy extends Parent,
		Target extends Parent>
	implements HooksProxy {

	@Getter
	private
	Class<Parent> parentClass;

	@Getter
	private
	Class<Proxy> proxyClass;

	@Getter
	private
	Class<Target> targetClass;

	protected
	HooksProxyImpl (
		Class<Parent> parentClass,
		Class<Proxy> proxyClass,
		Class<Target> targetClass) {

		this.parentClass =
			parentClass;

		this.proxyClass =
			proxyClass;

		this.targetClass =
			targetClass;

	}

}
