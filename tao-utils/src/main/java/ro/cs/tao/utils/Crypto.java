/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Utility class for encryption / decryption using AES
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public class Crypto {

    /**
     * Encrypts a string using a secret
     * @param strToEncrypt  The string to be encrypted
     * @param secret        The secret
     * @return      A Base64 representation of the encrypted string
     */
    public static String encrypt(String strToEncrypt, String secret) {
        if (strToEncrypt != null && secret != null) {
            try {
                SecretKeySpec key = createKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Decrypts an encrypted string given its secret
     * @param strToDecrypt  The Base64 representation of the encrypted string
     * @param secret        The secret used for encryption
     * @return      The decrypted string.
     */
    public static String decrypt(String strToDecrypt, String secret) {
        if (strToDecrypt != null && secret != null) {
            try {
                SecretKeySpec key = createKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            } catch (Exception e) {
                //e.printStackTrace();
                return strToDecrypt;
            }
        }
        return null;
    }

    public static String hash(List<String> inputs) {
        if (inputs != null && inputs.size() > 0) {
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            for (String input : inputs) {
                md5.update(input.getBytes());
            }
            return Base64.getEncoder().encodeToString(md5.digest());
        }
        return null;
    }

    private static SecretKeySpec createKey(String secret) {
        MessageDigest sha;
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
