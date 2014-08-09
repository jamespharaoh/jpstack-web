package wbs.platform.service.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class ServiceRec
	implements MinorRecord<ServiceRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	// details

	@DescriptionField
	String description;

	@TypeField
	ServiceTypeRec type;

	// compare to

	@Override
	public
	int compareTo (
			Record<ServiceRec> otherRecord) {

		ServiceRec other =
			(ServiceRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getParentObjectId (),
				other.getParentObjectId ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// object helper methods

	public static
	interface ServiceObjectHelperMethods {

		ServiceRec findOrCreate (
				Record<?> parent,
				String typeCode,
				String code);

	}

	// object helper implementation

	public static
	class ServiceObjectHelperImplementation
		implements ServiceObjectHelperMethods {

		@Inject
		Provider<ObjectManager> objectManagerProvider;

		@Inject
		Provider<ServiceObjectHelper> serviceHelperProvider;

		@Inject
		Provider<ServiceTypeObjectHelper> serviceTypeHelperProvider;

		@Inject
		Provider<ObjectTypeObjectHelper> objectTypeHelperProvider;

		@Override
		public
		ServiceRec findOrCreate (
				Record<?> parent,
				String typeCode,
				String code) {

			ObjectManager objectManager =
				objectManagerProvider.get ();

			ObjectTypeObjectHelper objectTypeHelper =
				objectTypeHelperProvider.get ();

			ServiceObjectHelper serviceHelper =
				serviceHelperProvider.get ();

			ServiceTypeObjectHelper serviceTypeHelper =
				serviceTypeHelperProvider.get ();

			// lookup existing service...

			ServiceRec service =
				serviceHelper
					.findByCode (
						parent,
						code);

			if (service != null)
				return service;

			// ...or create new service

			ObjectTypeRec parentType =
				objectTypeHelper.find (
					objectManager.getObjectTypeId (parent));

			ServiceTypeRec serviceType =
				serviceTypeHelper.findByCode (
					parentType,
					typeCode);

			service =
				objectManager.insert (
					new ServiceRec ()
						.setCode (code)
						.setDescription (serviceType.getDescription ())
						.setType (serviceType)
						.setParentObjectType (parentType)
						.setParentObjectId (parent.getId ()));

			return service;

		}

	}

	// object hooks

	public static
	class ServiceHooks
		extends AbstractObjectHooks<ServiceRec> {

		@Inject
		Database database;

		@Inject
		ObjectTypeDao objectTypeDao;

		@Inject
		ServiceTypeDao serviceTypeDao;

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		@PostConstruct
		public
		void init () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			List<ObjectTypeRec> objectTypes =
				objectTypeDao.findAll ();

			for (ObjectTypeRec objectType
					: objectTypes) {

				List<ServiceTypeRec> serviceTypes =
					serviceTypeDao.findByParentObjectType (
						objectType);

				if (serviceTypes.isEmpty ())
					continue;

				parentObjectTypeIds.add (
					objectType.getId ());

			}

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<ServiceRec> serviceHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<ServiceTypeRec> serviceTypes =
				serviceTypeDao.findByParentObjectType (
					parentType);

			for (ServiceTypeRec serviceType
					: serviceTypes) {

				serviceHelper.insert (
					new ServiceRec ()
						.setType (serviceType)
						.setCode (serviceType.getCode ())
						.setDescription (serviceType.getDescription ())
						.setParentObjectType (parentType)
						.setParentObjectId (parent.getId ()));

			}

		}

	}

}
