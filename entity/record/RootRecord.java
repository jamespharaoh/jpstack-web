package wbs.framework.entity.record;

public
interface RootRecord<ConcreteType extends Record<ConcreteType>>
	extends MajorRecord<ConcreteType> {

}
