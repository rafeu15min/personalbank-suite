package br.com.PersonalBank.common.config;

import java.util.Map;

/**
 * Representa a configuração para uma única instituição financeira.
 */
public class InstitutionConfig {

    public String name;
    public String organizationId;

    /**
     * Um mapa onde a chave é o tipo da API (ex: "accounts", "credit-cards")
     * e o valor é a configuração específica daquela API.
     */
    public Map<String, ApiConfig> api;
}