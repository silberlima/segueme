package com.silberlima.segueme.hierarquia.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EscopoTerritorialTest {

    private static final UUID DIOCESE = UUID.randomUUID();
    private static final UUID SETOR = UUID.randomUUID();
    private static final UUID PAROQUIA = UUID.randomUUID();

    private static final Territorio TERRITORIO = new Territorio(PAROQUIA, SETOR, DIOCESE);

    @Test
    void shouldContainAnyTerritorioWhenEscopoIsNacional() {
        EscopoTerritorial escopo = new EscopoTerritorial.Nacional();

        assertThat(escopo.contem(TERRITORIO)).isTrue();
        assertThat(escopo.contem(new Territorio(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
                .isTrue();
    }

    @Test
    void shouldContainTerritorioOfSameDioceseWhenEscopoIsDeDiocese() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeDiocese(DIOCESE);

        assertThat(escopo.contem(TERRITORIO)).isTrue();
    }

    @Test
    void shouldNotContainTerritorioOfAnotherDioceseWhenEscopoIsDeDiocese() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeDiocese(UUID.randomUUID());

        assertThat(escopo.contem(TERRITORIO)).isFalse();
    }

    @Test
    void shouldContainTerritorioOfSameSetorWhenEscopoIsDeSetor() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeSetor(SETOR);

        assertThat(escopo.contem(TERRITORIO)).isTrue();
    }

    @Test
    void shouldNotContainTerritorioOfAnotherSetorWhenEscopoIsDeSetor() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeSetor(UUID.randomUUID());

        assertThat(escopo.contem(TERRITORIO)).isFalse();
    }

    @Test
    void shouldContainTerritorioOfSameParoquiaWhenEscopoIsDeParoquia() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(PAROQUIA);

        assertThat(escopo.contem(TERRITORIO)).isTrue();
    }

    /** O caso que motiva o RNF-06: outra paróquia do mesmo setor não entra no escopo de paróquia. */
    @Test
    void shouldNotContainTerritorioOfAnotherParoquiaInTheSameSetorWhenEscopoIsDeParoquia() {
        EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(UUID.randomUUID());

        assertThat(escopo.contem(TERRITORIO)).isFalse();
    }
}
