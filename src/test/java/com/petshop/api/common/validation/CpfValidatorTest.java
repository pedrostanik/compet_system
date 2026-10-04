package com.petshop.api.common.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "529.982.247-25", "52998224725",   // same CPF, masked and digits only
            "111.444.777-35",
            "123.456.789-09",
            "000.000.001-91"                   // check digit computed as 10 -> 0 path
    })
    void acceptsValidCpfs(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "529.982.247-24",   // wrong second check digit
            "529.982.247-15",   // wrong first check digit
            "111.111.111-11",   // repeated digits pass the checksum but are invalid
            "000.000.000-00",
            "5299822472",       // 10 digits
            "529982247255",     // 12 digits
            "529.982.247/25",   // wrong separator
            "abc.def.ghi-jk",
            ""
    })
    void rejectsInvalidCpfs(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void nullIsLeftToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
