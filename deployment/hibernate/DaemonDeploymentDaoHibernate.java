package wbs.platform.deployment.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;

import wbs.platform.deployment.model.DaemonDeploymentDaoMethods;
import wbs.platform.deployment.model.DaemonDeploymentRec;

public
class DaemonDeploymentDaoHibernate
	extends HibernateDao
	implements DaemonDeploymentDaoMethods {

	@Override
	public
	List <DaemonDeploymentRec> findByHostNotDeleted (
			@NonNull String host) {

		return findMany (
			"findByHost",
			DaemonDeploymentRec.class,

			createCriteria (
				DaemonDeploymentRec.class)

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
