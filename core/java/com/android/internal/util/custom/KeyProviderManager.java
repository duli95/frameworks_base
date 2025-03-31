package com.android.internal.util.custom;

import android.util.Log;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for handling keybox providers.
 * @hide
 */
public class KeyProviderManager {
    private static final String TAG = "KeyProviderManager";

    private static String keybox = ReadFileData.readFile("keybox.xml");
    private static KeyboxData keyboxData = null;

    private static class KeyboxData {
        public String typeECPrivateKey;
        public String typeRSAPrivateKey;
        public String ecPrivateKey;
        public String rsaPrivateKey;
        public String[] ecCertificateChain;
        public String[] rsaCertificateChain;
    }

    private static void parseKeybox() {
        if (keybox == null || keybox.isEmpty()) {
            Log.e(TAG, "Keybox content is empty");
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(keybox.getBytes("UTF-8"));
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();

            KeyboxData data = new KeyboxData();

            NodeList keyList = doc.getElementsByTagName("Key");
            for (int i = 0; i < keyList.getLength(); i++) {
                Element keyElement = (Element) keyList.item(i);
                String algorithm = keyElement.getAttribute("algorithm").trim();

                Element privateKeyElement = (Element) keyElement.getElementsByTagName("PrivateKey").item(0);
                String privateKey = privateKeyElement.getTextContent().trim();

                Element certificateChainElement = (Element) keyElement.getElementsByTagName("CertificateChain").item(0);
                Element numberElem = (Element) certificateChainElement.getElementsByTagName("NumberOfCertificates").item(0);
                int numCertificates = Integer.parseInt(numberElem.getTextContent().trim());
                NodeList certificateNodes = certificateChainElement.getElementsByTagName("Certificate");
                List<String> certificateList = new ArrayList<>();
                for (int j = 0; j < certificateNodes.getLength() && j < numCertificates; j++) {
                    Element certElem = (Element) certificateNodes.item(j);
                    certificateList.add(replaceCertificate(certElem.getTextContent().trim()));
                }
                if ("ecdsa".equalsIgnoreCase(algorithm)) {
                    if(privateKey.contains("-----BEGIN EC PRIVATE KEY-----")){
                        data.typeECPrivateKey = "sec1";
                    } else {
                        data.typeECPrivateKey = "pkcs8";
                    }
                    data.ecPrivateKey = replaceCertificate(privateKey);
                    data.ecCertificateChain = certificateList.toArray(new String[0]);
                } else if ("rsa".equalsIgnoreCase(algorithm)) {
                    if(privateKey.contains("-----BEGIN RSA PRIVATE KEY-----")){
                        data.typeRSAPrivateKey = "pkcs1";
                    } else {
                        data.typeRSAPrivateKey = "pkcs8";
                    }
                    data.rsaPrivateKey = replaceCertificate(privateKey);
                    data.rsaCertificateChain = certificateList.toArray(new String[0]);
                }
            }
            keyboxData = data;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing keybox XML", e);
        }
    }

    private static String replaceCertificate(String certificate) {
        return certificate.replace("-----BEGIN CERTIFICATE-----", "")
                         .replace("-----END CERTIFICATE-----", "")
                         .replace("-----BEGIN EC PRIVATE KEY-----", "")
                         .replace("-----END EC PRIVATE KEY-----", "")
                         .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                         .replace("-----END RSA PRIVATE KEY-----", "")
                         .replace("-----BEGIN PRIVATE KEY-----", "")
                         .replace("-----END PRIVATE KEY-----", "")
                         .replaceAll("\\s+", "");
    }

    public static boolean isKeyboxTypeECPrivateKeySec1() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null && keyboxData.typeECPrivateKey != null && keyboxData.typeECPrivateKey.equals("sec1");
    }


    public static boolean isKeyboxTypeRSAPrivateKeyPkcs1() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null && keyboxData.typeRSAPrivateKey != null && keyboxData.typeRSAPrivateKey.equals("pkcs1");
    }

    public static boolean isKeyboxAvailable() {
        return keybox != null && !keybox.isEmpty();
    }

    public static String getEcPrivateKey() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null ? keyboxData.ecPrivateKey : null;
    }

    public static String getRsaPrivateKey() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null ? keyboxData.rsaPrivateKey : null;
    }

    public static String[] getEcCertificateChain() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null ? keyboxData.ecCertificateChain : null;
    }

    public static String[] getRsaCertificateChain() {
        if (keyboxData == null) {
            parseKeybox();
        }
        return keyboxData != null ? keyboxData.rsaCertificateChain : null;
    }
}
