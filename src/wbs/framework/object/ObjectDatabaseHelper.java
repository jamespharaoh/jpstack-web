package wbs.framework.object;

import java.util.List;

import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface ObjectDatabaseHelper<RecordType extends Record<RecordType>> {

	// getters

	ObjectModel <RecordType> model ();

	// setters

	ObjectDatabaseHelper <RecordType> model (
			ObjectModel <RecordType> model);

	// database

	RecordType find (
			Long id);

	List <RecordType> findMany (
			List <Long> ids);

	RecordType findByParentAndCode (
			GlobalId parentGlobalId,
			String code);

	RecordType findByParentAndIndex (
			GlobalId parentGlobalId,
			Long index);

	RecordType findByParentAndTypeAndCode (
			GlobalId parentGlobalId,
			String typeCode,
			String code);

	List <RecordType> findAll ();

	List <RecordType> findAllByParent (
			GlobalId parentGlobalId);

	List <RecordType> findByParentAndIndexRange (
			GlobalId parentGlobalId,
			Long indexStart,
			Long indexEnd);

	List <RecordType> findAllByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	RecordType insert (
			RecordType object);

	RecordType insertSpecial (
			RecordType object);

	RecordType update (
			RecordType object);

	<ObjectType extends EphemeralRecord <RecordType>>
	ObjectType remove (
			ObjectType object);

	RecordType lock (
			RecordType object);

}
