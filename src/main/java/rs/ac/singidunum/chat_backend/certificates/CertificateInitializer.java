package rs.ac.singidunum.chat_backend.certificates;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

@Component
@RequiredArgsConstructor
public class CertificateInitializer implements ApplicationRunner {

    private final CertificateProperties properties;
    private final CertificateService certificateService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Pokrecemo inicializaciju sertifikata");

        if (
                Files.exists(Path.of(properties.getCaCertPath())) &&
                Files.exists(Path.of(properties.getCaKeyPath())) &&
                Files.exists(Path.of(properties.getImCertPath())) &&
                Files.exists(Path.of(properties.getImKeyPath()))
        ) {
            System.out.println("All certificate and key files are detected");
            return;
        }

        System.out.println("Sertifikati ne postoje pokrece se generisanje sertifikata");

        KeyPair caKP = certificateService.generateRSAKeys(properties.getKeySize());
        X509Certificate caCert = certificateService.generateCACertificate(caKP, properties);
        KeyPair imKP = certificateService.generateRSAKeys(properties.getKeySize());
        X509Certificate imCert = certificateService.generateIMCertificate(imKP, caKP, caCert, properties);

        certificateService.writePem(Path.of(properties.getCaCertPath()), caCert);
        certificateService.writePem(Path.of(properties.getImCertPath()), imCert);
        certificateService.writePem(Path.of(properties.getCaKeyPath()), caKP.getPrivate());
        certificateService.writePem(Path.of(properties.getImKeyPath()), imKP.getPrivate());

        System.out.println("Generisani su sertifikati");
    }
}
