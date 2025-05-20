package com.birp.chat_backend.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Scanner;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CertificateInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(CertificateInitializer.class);

    @Value("${server.certificate.path:}")
    private String certificateFilePath;

    @Value("${server.privatekey.path:}")
    private String privateKeyFilePath;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Configure file paths with defaults if not specified
        if (certificateFilePath == null || certificateFilePath.isEmpty()) {
            certificateFilePath = "src/main/resources/cert/ca_cert.pem";
            logger.info("No certificate path provided. Using default path: {}", certificateFilePath);
        }

        if (privateKeyFilePath == null || privateKeyFilePath.isEmpty()) {
            privateKeyFilePath = "src/main/resources/cert/ca_key.pem";
            logger.info("No private key path provided. Using default path: {}", privateKeyFilePath);
        }

        File certFile = new File(certificateFilePath);
        File privateKeyFile = new File(privateKeyFilePath);

        // Ensure directories exist
        createDirectoryIfNeeded(certFile.getParentFile());
        createDirectoryIfNeeded(privateKeyFile.getParentFile());

        // If either cert or private key don't exist, generate both
        if (!certFile.exists() || !privateKeyFile.exists()) {
            if (!certFile.exists()) {
                logger.info("Certificate file not found at {}. Generating certificate...", certificateFilePath);
            }
            if (!privateKeyFile.exists()) {
                logger.info("Private key file not found at {}. Generating private key...", privateKeyFilePath);
            }
            generateDefaultCertificate(certFile, privateKeyFile);
        } else {
            logger.info("Certificate and private key files exist. Verifying...");
            
            // Load certificate and private key
            X509Certificate certificate = loadCertificateFromFile(certFile);
            PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile);
            
            // Verify certificate validity period
            try {
                certificate.checkValidity();
                logger.info("Certificate is valid.");
            } catch (Exception e) {
                throw new IllegalStateException("Certificate is not valid: " + e.getMessage(), e);
            }

            // Verify that the private key matches the certificate
            if (verifyKeyPair(certificate.getPublicKey(), privateKey)) {
                logger.info("Private key matches certificate. Initialization successful.");
            } else {
                logger.error("Private key does not match certificate. Regenerating both...");
                generateDefaultCertificate(certFile, privateKeyFile);
            }
        }
    }

    private boolean verifyKeyPair(PublicKey publicKey, PrivateKey privateKey) {
        try {
            // Create a test message
            byte[] testMessage = "Test Message for Key Verification".getBytes();
            
            // Sign with private key
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initSign(privateKey);
            signature.update(testMessage);
            byte[] signatureBytes = signature.sign();
            
            // Verify with public key
            signature.initVerify(publicKey);
            signature.update(testMessage);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            logger.error("Error verifying key pair: {}", e.getMessage());
            return false;
        }
    }

    private void createDirectoryIfNeeded(File dir) {
        if (dir != null && !dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Created directory: {}", dir.getAbsolutePath());
            } else {
                logger.warn("Failed to create directory: {}", dir.getAbsolutePath());
            }
        }
    }

    private X509Certificate loadCertificateFromFile(File certFile) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(certFile))) {
            Object object = pemParser.readObject();
            if (object instanceof X509CertificateHolder) {
                X509CertificateHolder holder = (X509CertificateHolder) object;
                return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
            } else {
                throw new IllegalStateException("The certificate file does not contain a valid X.509 certificate.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading certificate file: " + e.getMessage(), e);
        }
    }

    private PrivateKey loadPrivateKeyFromFile(File privateKeyFile) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            
            if (object instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                return converter.getKeyPair(pemKeyPair).getPrivate();
            } else {
                throw new IllegalStateException("The private key file does not contain a valid private key.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading private key file: " + e.getMessage(), e);
        }
    }

    private void generateDefaultCertificate(File certFile, File privateKeyFile) throws Exception {
        // Create parent directories if needed
        createDirectoryIfNeeded(certFile.getParentFile());
        createDirectoryIfNeeded(privateKeyFile.getParentFile());

        // Generate RSA key pair
        KeyPairGenerator rsaKPG = KeyPairGenerator.getInstance("RSA");
        rsaKPG.initialize(4096);
        KeyPair rsaKP = rsaKPG.generateKeyPair();

        // Certificate details
        String issuerParams = "CN=KIBP, O=KIBP, C=Serbia";
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 300 * 1000);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // valid for 1 year

        // Build a self-signed certificate
        X500Name issuer = new X500Name(issuerParams);
        X500Name subject = issuer;
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, notBefore, notAfter, subject, rsaKP.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(rsaKP.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);

        // Write the certificate to the file in PEM format
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(certFile))) {
            pemWriter.writeObject(certificate);
            logger.info("Certificate written to: {}", certFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing certificate: {}", e.getMessage());
            throw e;
        }

        // Write the private key to the file in PEM format
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(privateKeyFile))) {
            pemWriter.writeObject(rsaKP.getPrivate());
            logger.info("Private key written to: {}", privateKeyFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing private key: {}", e.getMessage());
            throw e;
        }

        logger.info("Default CA certificate and private key generated successfully.");
    }


    public static String getCertificateString(String certFile) {
        try {     
            FileInputStream fis = new FileInputStream(certFile);
            Scanner s = new Scanner(fis);
            String cert = "";

            while(s.hasNext()){
                cert += s.nextLine();
            }

            s.close();
            fis.close();
            return cert;
        } catch (Exception e) {
            System.out.println("Greska prilikom citanja sertifikata");
            return null;
        }
    }
}
