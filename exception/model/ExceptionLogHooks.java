package wbs.platform.exception.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class ExceptionLogHooks
	extends AbstractObjectHooks<ExceptionLogRec> {

	// dependencies

	@Inject
	ExceptionLogDao exceptionLogDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object searchObject) {

		ExceptionLogSearch search =
			(ExceptionLogSearch) searchObject;

		return exceptionLogDao.searchIds (
			search);

	}

}