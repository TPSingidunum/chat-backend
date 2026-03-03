package rs.ac.singidunum.chat_backend.certificates;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.cert")
public class CertificateProperties {
    private String caCertPath;
    private String caKeyPath;
    private String imCertPath;
    private String imKeyPath;
    private String outputDir = "certs";
    private int keySize = 4096;
    private int caValidDuration;
    private int imValidDuration;
    private DnProperties caDn;
    private DnProperties imDn;
}
