package com.android.internal.util.custom;

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import android.util.Log;

public class ConvertRSAPrivateKey {
    private static final String TAG = "ConvertRSAPrivateKey";

    public static PrivateKey convertRSAPrivateKey(String rsaKey) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(rsaKey);

        if (keyBytes[0] == 0x30) {
            if (KeyProviderManager.isKeyboxTypeRSAPrivateKeyPkcs1()) {
                return generateRSAPrivateKeyFromPKCS1(keyBytes);
            } else {
                return generateRSAPrivateKeyFromPKCS8(keyBytes);
            }
        } else {
            throw new IllegalArgumentException("Invalid RSA key format.");
        }
    }

    private static PrivateKey generateRSAPrivateKeyFromPKCS8(byte[] keyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        return keyFactory.generatePrivate(keySpec);
    }

    private static PrivateKey generateRSAPrivateKeyFromPKCS1(byte[] keyBytes) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(keyBytes);
        int sequence = in.read();
        if (sequence != 0x30) {
            throw new IOException("Invalid ASN.1 format");
        }

        readASN1Length(in);

        int version = readASN1Integer(in).intValueExact();
        if (version != 0) {
            throw new IOException("Unsupported RSA key version");
        }

        BigInteger modulus = readASN1Integer(in);
        BigInteger publicExponent = readASN1Integer(in);
        BigInteger privateExponent = readASN1Integer(in);
        BigInteger prime1 = readASN1Integer(in);
        BigInteger prime2 = readASN1Integer(in);
        BigInteger exponent1 = readASN1Integer(in);
        BigInteger exponent2 = readASN1Integer(in);
        BigInteger coefficient = readASN1Integer(in);

        RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
                modulus, publicExponent, privateExponent, prime1, prime2, exponent1, exponent2, coefficient);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    private static int readASN1Length(ByteArrayInputStream in) throws IOException {
        int length = in.read();
        if (length < 0) throw new IOException("Invalid ASN.1 length");
        if (length > 127) {
            int numBytes = length & 0x7F;
            length = 0;
            for (int i = 0; i < numBytes; i++) {
                length = (length << 8) | in.read();
            }
        }
        return length;
    }

    private static BigInteger readASN1Integer(ByteArrayInputStream in) throws IOException {
        if (in.read() != 0x02) {
            throw new IOException("Invalid ASN.1 integer format");
        }
        int length = readASN1Length(in);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return new BigInteger(1, bytes);
    }
}
