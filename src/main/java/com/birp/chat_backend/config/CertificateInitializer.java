package com.birp.chat_backend.config;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CertificateInitializer implements ApplicationRunner {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${spring.certificate.path}")
    private String certificatePath;

    private static final String defaultCertificatePath = "src/main/resources/certs/cert.pem";
    private static final String defaultPrivateKeyPath = "src/main/resources/certs/pri.pem";
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //System.out.println("Certificate path" + this.certificatePath);
        if (this.certificatePath == null || certificatePath.isEmpty()) {
            this.certificatePath = defaultCertificatePath;
            System.out.println("Certificate not provided, will create a default cert at "+
            " path : " + this.certificatePath);
        }

        // Ucitaj ili napravi sertifikat

        File certificate = new File(certificatePath);
        if (!certificate.exists()) {
            System.out.println("Certificate does not exist at specified path, creating one");
            generateSertificate(certificate);
        }
    }

    public void generateSertificate(File certificate) throws Exception {

        // Garantuj da postoji lokacija za cuvanje fajla
        certificate.getParentFile().mkdirs();

        //Generisati RSA kljuceve potrebne za sertifikat
        KeyPairGenerator rsaKPG = KeyPairGenerator.getInstance("RSA");
        rsaKPG.initialize(4096);
        KeyPair rsaKP = rsaKPG.generateKeyPair();

        // Certificate details
        String issuerParams = "CN=BIRP, O=Singidunum, C=Serbia";
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
        Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        // Build Certificate
        X500Name issuer = new X500Name(issuerParams);
        X500Name subject = issuer;
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            issuer,
            serial,
            notBefore, 
            notAfter,
            subject,
            rsaKP.getPublic()
        );

        ContentSigner singer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
            .setProvider("BC")
            .build(rsaKP.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(singer);
        X509Certificate finalCertificate = new JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certHolder);

        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(certificate))) {
           pemWriter.writeObject(finalCertificate); 
        } catch (Exception e) {
            System.out.println("Error while saving certificate");
            e.printStackTrace();
        }

        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(new File(defaultPrivateKeyPath)))) {
           pemWriter.writeObject(rsaKP.getPrivate()); 
        } catch (Exception e) {
            System.out.println("Error while saving certificate");
            e.printStackTrace();
        }
    }

}
