package com.birp.chat_backend.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CertificateUtil {
    private static final Logger logger = LoggerFactory.getLogger(CertificateUtil.class);
    
    @Value("${server.certificate.path:src/main/resources/cert/ca_cert.pem}")
    private String certificatePath;
    
    @Value("${server.privatekey.path:src/main/resources/cert/ca_key.pem}")
    private String privateKeyPath;
    
    private PrivateKey serverPrivateKey;
    
    /**
     * Initialize the server private key after properties are set
     */
    public void init() {
        try {
            this.serverPrivateKey = loadPrivateKeyFromFile(new File(privateKeyPath));
            logger.info("Server private key loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading server private key: {}", e.getMessage());
        }
    }
    
    /**
     * Extract public key from X.509 certificate string
     */
    public PublicKey extractPublicKeyFromCertificate(String certificatePem) throws Exception {
        try {
            // Clean up PEM format if needed
            String cleanCert = certificatePem
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");
            
            byte[] certBytes = Base64.getDecoder().decode(cleanCert);
            
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(
                    new ByteArrayInputStream(certBytes));
            
            return cert.getPublicKey();
        } catch (Exception e) {
            logger.error("Error extracting public key: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Encrypt data with public key
     */
    public String encryptWithPublicKey(String data, PublicKey publicKey) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Error encrypting with public key: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Decrypt data with server's private key
     */
    public String decryptWithServerPrivateKey(String encryptedData) throws Exception {
        if (serverPrivateKey == null) {
            init(); // Lazy initialization if needed
        }
        
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error decrypting with server private key: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Load private key from file
     */
    private PrivateKey loadPrivateKeyFromFile(File privateKeyFile) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
            
            if (object instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                return converter.getKeyPair(pemKeyPair).getPrivate();
            } else {
                throw new IllegalStateException("The private key file does not contain a valid private key");
            }
        }
    }
}