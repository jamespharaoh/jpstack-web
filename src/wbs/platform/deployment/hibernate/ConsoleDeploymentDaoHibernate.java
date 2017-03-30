package wbs.platform.deployment.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;

import wbs.platform.deployment.model.ConsoleDeploymentDaoMethods;
import wbs.platform.deployment.model.ConsoleDeploymentRec;

public
class ConsoleDeploymentDaoHibernate
	extends HibernateDao
	implements ConsoleDeploymentDaoMethods {

	@Override
	public
	List <ConsoleDeploymentRec> findByHostNotDeleted (
			@NonNull String host) {

		return findMany (
			"findByHost",
			ConsoleDeploymentRec.class,

			createCriteria (
				ConsoleDeploymentRec.class)

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
