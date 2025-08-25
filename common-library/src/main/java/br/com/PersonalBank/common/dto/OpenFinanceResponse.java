package br.com.PersonalBank.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenFinanceResponse<T> {

    @JsonProperty("data")
    public T data;

    @JsonProperty("links")
    public Links links;

    @JsonProperty("meta")
    public Meta meta;

    public record Links(
            @JsonProperty("self") String self,
            @JsonProperty("first") String first,
            @JsonProperty("prev") String prev,
            @JsonProperty("next") String next,
            @JsonProperty("last") String last) {
    }

    public record Meta(
            @JsonProperty("totalRecords") int totalRecords,
            @JsonProperty("totalPages") int totalPages) {
    }
}