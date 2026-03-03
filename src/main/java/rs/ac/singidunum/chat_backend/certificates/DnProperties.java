package rs.ac.singidunum.chat_backend.certificates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnProperties {
    private String commonName;
    private String orgUnit;
    private String org;
    private String locality;
    private String state;
    private String country;

    public String toX500Principal() {
        return String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s",commonName, orgUnit, org, locality, state, country);
    }
}
