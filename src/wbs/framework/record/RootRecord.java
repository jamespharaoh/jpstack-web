package wbs.framework.record;

public
interface RootRecord<ConcreteType extends Record<ConcreteType>>
	extends MajorRecord<ConcreteType> {

}
