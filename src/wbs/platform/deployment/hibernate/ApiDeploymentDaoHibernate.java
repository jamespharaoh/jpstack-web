package wbs.platform.deployment.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;

import wbs.platform.deployment.model.ApiDeploymentDaoMethods;
import wbs.platform.deployment.model.ApiDeploymentRec;

public
class ApiDeploymentDaoHibernate
	extends HibernateDao
	implements ApiDeploymentDaoMethods {

	@Override
	public
	List <ApiDeploymentRec> findByHostNotDeleted (
			@NonNull String host) {

		return findMany (
			"findByHost",
			ApiDeploymentRec.class,

			createCriteria (
				ApiDeploymentRec.class)

			.add (
				Restrictions.eq (
					"host",
					host))

			.add (
				Restrictions.eq (
					"deleted",
					false))

		);

	}


}
