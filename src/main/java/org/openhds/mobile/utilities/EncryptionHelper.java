package org.openhds.mobile.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class EncryptionHelper {

	private static final String ENCRYPTION_PASSWORD_KEY = "database-password";
	private static final String ENCRYPTION_SHARED_PREF = "openhds-provider";

	private static final String ENCRYPTED_FLAG = ".encrypted";

	private static Cipher aesCipher;

	/*
	 * EncryptionHelper Generates a 128 bit AES key and stores it in Android's
	 * SharedPreferences. This Key is unique to each installed instance of the
	 * application.
	 */

	public EncryptionHelper() {

	}

	private static Key getOrGenerateKey(Context context)
			throws NoSuchAlgorithmException {

		String stringKey;
		SecretKey key;
		SharedPreferences sp;

		sp = context.getSharedPreferences(ENCRYPTION_SHARED_PREF,
				Context.MODE_PRIVATE);

		stringKey = sp.getString(ENCRYPTION_PASSWORD_KEY, "");

		if (stringKey.isEmpty()) {
			key = KeyGenerator.getInstance("AES").generateKey();
			stringKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);

			sp.edit().putString(ENCRYPTION_PASSWORD_KEY, stringKey).commit();
		} else {
			byte[] encodedKey = Base64.decode(stringKey, Base64.DEFAULT);
			key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		}

		return key;
	}

	public static void encryptFile(File file, Context context) {

		File inFile = file;
		File outFile = getFileWithEncryptedFlag(file);

		if (null != file && !isEncrypted(file)) {
			startCipher(Cipher.ENCRYPT_MODE, context, inFile, outFile);
		}

	}

	public static void decryptFile(File file, Context context) {

		File inFile = getFileWithEncryptedFlag(file);
		File outFile = file;

		if (null != file && isEncrypted(file)) {
			startCipher(Cipher.DECRYPT_MODE, context, inFile, outFile);
		}
	}

	public static void encryptFiles(List<File> files, Context context) {

		for (File file : files) {
			encryptFile(file, context);
		}

	}

	public static void decryptFiles(List<File> files, Context context) {

		for (File file : files) {
			decryptFile(file, context);
		}
	}

	private static void readAndWriteFile(File inFile, File outFile,
			Cipher cipher) throws IOException {

		byte[] fileInBytes = new byte[(int) inFile.length()];

		FileInputStream fileInputStream = new FileInputStream(inFile);
		fileInputStream.read(fileInBytes);
		fileInputStream.close();

		CipherOutputStream cipherOutputStream = new CipherOutputStream(
				new FileOutputStream(outFile), aesCipher);
		cipherOutputStream.write(fileInBytes);
		cipherOutputStream.close();

		inFile.delete();

	}

	private static void startCipher(int cipherMode, Context context,
			File inFile, File outFile) {

		if (!inFile.exists()) {
			return;
		}

		try {
			aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(cipherMode, getOrGenerateKey(context));
			readAndWriteFile(inFile, outFile, aesCipher);

		} catch (InvalidKeyException e) {
			MessageUtils.showLongToast(context, "InvalidKeyException: " + e);
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			MessageUtils.showLongToast(context, "NoSuchAlgorithmException: "
					+ e);
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			MessageUtils.showLongToast(context, "NoSuchPaddingException: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			MessageUtils.showLongToast(context, "IOException: " + e);
			e.printStackTrace();
		}

	}

	private static File getFileWithEncryptedFlag(File file) {
		return new File(file.getAbsolutePath().concat(ENCRYPTED_FLAG));
	}

	private static boolean isEncrypted(File file) {

		if (getFileWithEncryptedFlag(file).exists()) {
			return true;
		}

		if (file.exists()) {
			return false;
		}

		return false;
	}

}
