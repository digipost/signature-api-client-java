package no.digipost.signature.client;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum Certificates {

    TEST(
            "test/Buypass_Class_3_Test4_CA_3.cer",
            "test/Buypass_Class_3_Test4_Root_CA.cer",
            "test/commfides_test_ca.cer",
            "test/commfides_test_root_ca.cer",
            "test/digipost_test_root_ca.pem"
    ),
    PRODUCTION(
            "prod/BPClass3CA3.cer",
            "prod/BPClass3RootCA.cer",
            "prod/commfides_ca.cer",
            "prod/commfides_root_ca.cer"
    );

    final List<String> certificatePaths;

    Certificates(String ... certificatePaths) {
        this.certificatePaths = Stream.of(certificatePaths)
                .map("classpath:/no/digipost/signature/client/certificates/"::concat)
                .collect(toList());
    }

}
