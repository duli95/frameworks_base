package com.android.internal.util.custom;

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @hide
 */
public class ConvertECPrivateKey {
    
    public static PrivateKey convertECPrivateKey(String ecKey) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(ecKey);

        if (keyBytes[0] == 0x30) {
            if(KeyProviderManager.isKeyboxTypeECPrivateKeySec1()) {
                return generateECPrivateKeyFromSEC1(keyBytes);
            } else {
                return generateECPrivateKeyFromPKCS8(keyBytes);
            }
        } else {
            throw new IllegalArgumentException("Invalid EC key format.");
        }
    }

    private static PrivateKey generateECPrivateKeyFromPKCS8(byte[] keyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        return keyFactory.generatePrivate(keySpec);
    }

    private static PrivateKey generateECPrivateKeyFromSEC1(byte[] keyBytes) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(keyBytes);

        int sequence = in.read();
        if (sequence != 0x30) {
            throw new IOException("Invalid ASN.1 format");
        }

        readASN1Length(in);

        int version = readASN1Integer(in).intValueExact();
        if (version != 1) {
            throw new IOException("Unsupported EC key version");
        }

        BigInteger privateKeyValue = readASN1OctetString(in);

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecSpec = parameters.getParameterSpec(ECParameterSpec.class);

        ECPrivateKeySpec keySpec = new ECPrivateKeySpec(privateKeyValue, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
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

    private static BigInteger readASN1OctetString(ByteArrayInputStream in) throws IOException {
        if (in.read() != 0x04) {
            throw new IOException("Invalid ASN.1 octet string format");
        }
        int length = readASN1Length(in);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return new BigInteger(1, bytes);
    }
}
