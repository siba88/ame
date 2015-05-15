package util;
import java.util.Random;

public class ElementHelper {

	public static String toUpperCase(String value) {
		return value.toUpperCase();
	}

	public static String generateRandomName() {
		return "Professor " + new Random().nextInt(9999999);
	}

}
