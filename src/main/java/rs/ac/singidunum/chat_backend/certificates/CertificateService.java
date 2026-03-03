package rs.ac.singidunum.chat_backend.certificates;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class CertificateService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public X509Certificate generateCACertificate(KeyPair kp, CertificateProperties properties) throws NoSuchAlgorithmException, CertIOException, OperatorCreationException, CertificateException {
        // Parametri za sertifikat
        X500Name subject = new X500Name(properties.getCaDn().toX500Principal());
        BigInteger serial = generateSerialNumber();
        Date notBefore = Date.from(Instant.now());
        Date notAfter = Date.from(Instant.now().plus(properties.getCaValidDuration(), ChronoUnit.DAYS));

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
            subject, serial, notBefore, notAfter, subject, kp.getPublic()
        );

        // Dodavanje privilegija sertifikatu
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(1));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(kp.getPublic()));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build((kp.getPrivate()));

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(builder.build(signer));
    }

    public X509Certificate generateIMCertificate(KeyPair IMkp, KeyPair CAkp, X509Certificate caCert, CertificateProperties properties) throws NoSuchAlgorithmException, CertIOException, OperatorCreationException, CertificateException {

        // Parametri za sertifikat
        X500Name issuer = new X500Name(properties.getCaDn().toX500Principal());
        X500Name subject = new X500Name(properties.getImDn().toX500Principal());
        BigInteger serial = generateSerialNumber();
        Date notBefore = Date.from(Instant.now());
        Date notAfter = Date.from(Instant.now().plus(properties.getImValidDuration(), ChronoUnit.DAYS));

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, IMkp.getPublic()
        );

        // Dodavanje privilegija sertifikatu
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(IMkp.getPublic()));
        builder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(caCert));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build((CAkp.getPrivate()));

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(builder.build(signer));
    }

    public KeyPair generateRSAKeys(int keyLength) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keyLength, new SecureRandom());
        return kpg.generateKeyPair();
    }

    private BigInteger generateSerialNumber() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return new BigInteger(bytes).abs();
    }

    public void writePem(Path target, Object object) {
        try(JcaPEMWriter pw = new JcaPEMWriter(new FileWriter(target.toFile()))) {
            pw.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
