package br.com.PersonalBank.common.client;

import br.com.PersonalBank.common.config.InstitutionConfig;
import br.com.PersonalBank.common.config.InstitutionConfigProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class ApiClientProvider {

        @Inject
        InstitutionConfigProvider configProvider;

        /**
         * Método genérico e robusto para construir qualquer tipo de cliente REST.
         * Retorna um Optional, que estará vazio se a configuração da URL não for
         * encontrada.
         *
         * @param clientInterface A classe da interface do cliente a ser construída.
         * @param institutionKey  A chave da instituição (ex: "NUBANK").
         * @param apiFamily       A chave da família de API (ex: "investments").
         * @param <T>             O tipo da interface do cliente.
         * @return Um Optional contendo o cliente REST se a URL foi encontrada, ou um
         *         Optional vazio caso contrário.
         */
        public <T> Optional<T> buildClient(Class<T> clientInterface, String institutionKey, String apiFamily) {

                // Procura pela configuração da instituição. Se não encontrar, retorna vazio.
                InstitutionConfig institutionConfig = configProvider.getInstitution(institutionKey);
                if (institutionConfig == null) {
                        return Optional.empty();
                }

                // Procura pela URL específica. Se não encontrar, retorna vazio.
                String baseUrl = Optional.ofNullable(institutionConfig.api.get(apiFamily))
                                .map(apiConfig -> apiConfig.url)
                                .orElse(null);

                if (baseUrl == null || baseUrl.isBlank()) {
                        return Optional.empty();
                }

                // Se encontrou a URL, constrói o cliente e o retorna dentro de um Optional.
                T client = RestClientBuilder.newBuilder()
                                .baseUri(URI.create(baseUrl))
                                .build(clientInterface);

                return Optional.of(client);
        }
}