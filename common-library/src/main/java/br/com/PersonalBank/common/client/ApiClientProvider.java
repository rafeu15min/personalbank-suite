package br.com.PersonalBank.common.client;

// ** MUDANÇA PRINCIPAL **
import br.com.PersonalBank.common.config.InstitutionConfigProvider;
import br.com.PersonalBank.common.config.InstitutionConfig;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class ApiClientProvider {

        // ** MUDANÇA PRINCIPAL **
        // Injeta o nosso novo provedor de configuração manual
        @Inject
        InstitutionConfigProvider configProvider;

        public <T> T buildClient(Class<T> clientInterface, String institutionKey, String apiFamily) {

                // ** MUDANÇA PRINCIPAL **
                // Pega a configuração do novo provedor
                InstitutionConfig institutionConfig = Optional.ofNullable(configProvider.getInstitution(institutionKey))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Nenhuma configuração encontrada para a instituição: "
                                                                + institutionKey));

                String baseUrl = Optional.ofNullable(institutionConfig.api.get(apiFamily))
                                .map(apiConfig -> apiConfig.url)
                                .orElseThrow(() -> new IllegalArgumentException("Nenhuma configuração de API '"
                                                + apiFamily + "' encontrada para a instituição: " + institutionKey));

                return RestClientBuilder.newBuilder()
                                .baseUri(URI.create(baseUrl))
                                .build(clientInterface);
        }
}