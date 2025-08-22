package br.com.PersonalBank.common.config;

import org.eclipse.microprofile.config.Config;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class InstitutionConfigProvider {

    @Inject
    Config config; // Injeta a API de configuração padrão do MicroProfile

    private Map<String, InstitutionConfig> institutionsMap;

    /**
     * Este método é executado uma vez quando a aplicação inicia.
     * Ele lê todas as propriedades e constrói o nosso mapa de configurações.
     */
    @PostConstruct
    void loadConfigurations() {
        institutionsMap = new HashMap<>();
        final String prefix = "personalbank.institutions.";

        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith(prefix)) {
                String keyPath = propertyName.substring(prefix.length());
                String[] parts = keyPath.split("\\.", 2);

                if (parts.length < 2)
                    continue;

                String institutionKey = parts[0];
                String attribute = parts[1];

                InstitutionConfig institutionConfig = institutionsMap.computeIfAbsent(institutionKey,
                        k -> new InstitutionConfig());

                String value = config.getValue(propertyName, String.class);
                if ("name".equals(attribute)) {
                    institutionConfig.name = value;
                } else if ("organizationId".equals(attribute)) {
                    institutionConfig.organizationId = value;
                } else if (attribute.startsWith("api.")) {
                    String[] apiParts = attribute.split("\\.");
                    if (apiParts.length == 3 && "url".equals(apiParts[2])) {
                        String apiFamily = apiParts[1];
                        if (institutionConfig.api == null) {
                            institutionConfig.api = new HashMap<>();
                        }
                        ApiConfig apiConfig = institutionConfig.api.computeIfAbsent(apiFamily, k -> new ApiConfig());
                        apiConfig.url = value;
                    }
                }
            }
        }
    }

    /**
     * Retorna a configuração para uma instituição específica.
     * 
     * @param institutionKey A chave da instituição (ex: "NUBANK")
     * @return A configuração encontrada ou null se não existir.
     */
    public InstitutionConfig getInstitution(String institutionKey) {
        return institutionsMap.get(institutionKey);
    }
}