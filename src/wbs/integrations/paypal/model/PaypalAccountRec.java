package wbs.integrations.paypal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class PaypalAccountRec
	implements MajorRecord<PaypalAccountRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	@SimpleField (
		nullable = true)
	String username;

	@SimpleField (
		nullable = true)
	String password;

	@SimpleField (
		nullable = true)
<<<<<<< HEAD:src/wbs/integrations/paypal/model/PaypalAccountRec.java
	String signature;

	@SimpleField (
		nullable = true)
	String apiId;

	@SimpleField (
		nullable = true)
	String serviceEndpointPaypalApi;

	@SimpleField (
		nullable = true)
	String serviceEndpointPaypalApiAa;

	@SimpleField (
		nullable = true)
	String serviceEndpointPermissions;

	@SimpleField (
		nullable = true)
	String serviceEndpointAdaptivePayments;

	@SimpleField (
		nullable = true)
	String serviceEndpointAdaptiveAccounts;

	@SimpleField (
		nullable = true)
	String serviceEndpointInvoice;
||||||| merged common ancestors
	String clientSecret;
=======
	String signature;

	@SimpleField (
		nullable = true)
	String appId;

	@SimpleField (
		nullable = true)
	String serviceEndpointPaypalApi;

	@SimpleField (
		nullable = true)
	String serviceEndpointPaypalApiAa;

	@SimpleField (
		nullable = true)
	String serviceEndpointPermissions;

	@SimpleField (
		nullable = true)
	String serviceEndpointAdaptivePayments;

	@SimpleField (
		nullable = true)
	String serviceEndpointAdaptiveAccounts;

	@SimpleField (
		nullable = true)
	String serviceEndpointInvoice;
>>>>>>> master:src/wbs/integrations/paypal/model/PaypalAccountRec.java

	// compare to

	@Override
	public
	int compareTo (
			Record<PaypalAccountRec> otherRecord) {

		PaypalAccountRec other =
			(PaypalAccountRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
