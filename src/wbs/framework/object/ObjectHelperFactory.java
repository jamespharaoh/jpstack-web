package wbs.framework.object;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.context.UninitializedComponentFactory;

@Accessors (fluent = true)
@Log4j
public
class ObjectHelperFactory
	implements UninitializedComponentFactory {

	// dependencies

	@Inject
	ObjectHelperBuilder objectHelperManager;

	// properties

	@Getter @Setter
	String objectName;

	@Getter @Setter
	Class<?> objectHelperClass;

	// implementation

	@Override
	public
	Object makeComponent () {

		log.debug (
			stringFormat (
				"Getting object helper for %s",
				objectName));

		try {

			return objectHelperManager.forObjectName (
				objectName);

		} catch (IllegalArgumentException exception) {

			log.error (
				stringFormat (
					"No object helper for %s",
					objectName),
				exception);

			return null;

		} catch (Exception exception) {

			log.error (
				stringFormat (
					"Error getting object helper for %s",
					objectName),
				exception);

			return null;

		}

	}

}
