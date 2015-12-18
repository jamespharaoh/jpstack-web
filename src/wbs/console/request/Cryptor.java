package wbs.console.request;

public
interface Cryptor {

	String encryptInt (
			int input);

	int decryptInt (
			String input);
}
