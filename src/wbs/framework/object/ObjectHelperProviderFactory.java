package wbs.framework.object;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.context.BeanFactory;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.hibernate.HibernateHelperProviderBuilder;

@Accessors (fluent = true)
@Log4j
public
class ObjectHelperProviderFactory
	implements BeanFactory {

	// dependencies

	@Inject
	EntityHelper entityHelper;

	@Inject
	Provider<HibernateHelperProviderBuilder> hibernateHelperProviderBuilder;

	// properties

	@Getter @Setter
	String objectName;

	@Getter @Setter
	ObjectHooks<?> objectHooks;

	// implementation

	@PostConstruct
	public
	void init () {
	}

	@Override
	public
	Object instantiate () {

		log.debug (
			stringFormat (
				"Getting object helper provider for %s",
				objectName));

		Model model =
			entityHelper.modelsByName ().get (
				objectName);

		if (model == null) {

			throw new RuntimeException (
				stringFormat (
					"Can't find model for object name %s",
					objectName));

		}

		return hibernateHelperProviderBuilder.get ()
			.model (model)
			.objectHooks (objectHooks)
			.build ();

	}

}
