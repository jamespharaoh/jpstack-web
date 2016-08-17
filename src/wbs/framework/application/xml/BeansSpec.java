package wbs.framework.application.xml;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.ComponentDefinition;
import wbs.framework.application.context.ComponentFactory;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("beans")
@Log4j
public
class BeansSpec {

	@DataChildren (direct = true)
	@Getter @Setter
	List<BeansBeanSpec> beans;

	@SneakyThrows (Exception.class)
	public
	int register (
			ApplicationContext applicationContext) {

		int errors = 0;

		for (BeansBeanSpec bean
				: beans) {

			Class<?> beanClass = null;

			try {

				beanClass =
					Class.forName (bean.className ());

			} catch (ClassNotFoundException exception) {

				log.error (
					stringFormat (
						"No such class %s specified as bean class for %s",
						bean.className (),
						bean.name ()));

				errors ++;

			}

			Class <? extends ComponentFactory> factoryClass = null;

			try {

				factoryClass =
					bean.factoryClassName () != null
						? Class.forName (bean.factoryClassName ())
							.asSubclass (ComponentFactory.class)
						: null;

			} catch (ClassNotFoundException exception) {

				log.error (
					stringFormat (
						"No such class %s specified as factory for %s",
						bean.factoryClassName (),
						bean.name ()));

				errors ++;

			} catch (ClassCastException exception) {

				log.error (
					stringFormat (
						"Factory class %s for %s is not a BeanFactory",
						bean.factoryClassName (),
						bean.name ()));

				errors ++;

			}

			if (errors > 0)
				continue;

			ComponentDefinition beanDefinition =
				new ComponentDefinition ()

				.name (
					bean.name ())

				.beanClass (
					beanClass)

				.factoryClass (
					factoryClass)

				.hide (
					bean.hide ())

				.scope (
					bean.scope ());

			for (BeansBeanPropertySpec beanProperty
					: bean.properties ()) {

				errors +=
					beanProperty.register (
						beanDefinition);

			}

			applicationContext.registerBeanDefinition (
				beanDefinition);

		}

		return errors;

	}

}
