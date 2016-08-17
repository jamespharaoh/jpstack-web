package wbs.console.request;

public
interface Cryptor {

	String encryptInteger (
			Long input);

	Long decryptInteger (
			String input);

}
