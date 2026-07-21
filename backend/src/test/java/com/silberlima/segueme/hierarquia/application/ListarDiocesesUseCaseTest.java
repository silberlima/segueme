package com.silberlima.segueme.hierarquia.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.application.dto.DioceseArvoreResult;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListarDiocesesUseCaseTest {

    @Mock
    private DioceseRepository dioceseRepository;

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void shouldAssembleTreeWithSetoresAndParoquiasWhenNacional() {
        Diocese diocese = new Diocese("Diocese Central");
        Setor setor = new Setor("Setor 1", diocese);
        Paroquia paroquia = new Paroquia("Paróquia São José", setor);
        atribuirId(diocese, UUID.randomUUID());
        atribuirId(setor, UUID.randomUUID());
        atribuirId(paroquia, UUID.randomUUID());

        when(dioceseRepository.findByDeletedFalse(pageable)).thenReturn(new PageImpl<>(List.of(diocese)));
        when(setorRepository.findByDiocese_IdInAndDeletedFalse(anyList())).thenReturn(List.of(setor));
        when(paroquiaRepository.findBySetor_IdInAndDeletedFalse(anyList())).thenReturn(List.of(paroquia));

        ListarDiocesesUseCase useCase =
                new ListarDiocesesUseCase(dioceseRepository, setorRepository, paroquiaRepository);
        Page<DioceseArvoreResult> resultado = useCase.execute(new EscopoTerritorial.Nacional(), pageable);

        assertThat(resultado.getContent()).hasSize(1);
        DioceseArvoreResult dioceseResult = resultado.getContent().get(0);
        assertThat(dioceseResult.nome()).isEqualTo("Diocese Central");
        assertThat(dioceseResult.numParoquias()).isEqualTo(1);
        assertThat(dioceseResult.setores()).hasSize(1);
        assertThat(dioceseResult.setores().get(0).paroquias()).extracting("nome")
                .containsExactly("Paróquia São José");
    }

    @Test
    void shouldReturnOnlyOwnDioceseWhenDeDiocese() {
        UUID dioceseId = UUID.randomUUID();
        Diocese diocese = new Diocese("Diocese Central");

        when(dioceseRepository.findByIdAndDeletedFalse(dioceseId)).thenReturn(Optional.of(diocese));
        when(setorRepository.findByDiocese_IdInAndDeletedFalse(anyList())).thenReturn(List.of());

        ListarDiocesesUseCase useCase =
                new ListarDiocesesUseCase(dioceseRepository, setorRepository, paroquiaRepository);
        Page<DioceseArvoreResult> resultado = useCase.execute(new EscopoTerritorial.DeDiocese(dioceseId), pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Diocese Central");
    }

    @Test
    void shouldReturnEmptyWhenDeDioceseNotFound() {
        UUID dioceseId = UUID.randomUUID();
        when(dioceseRepository.findByIdAndDeletedFalse(dioceseId)).thenReturn(Optional.empty());

        ListarDiocesesUseCase useCase =
                new ListarDiocesesUseCase(dioceseRepository, setorRepository, paroquiaRepository);
        Page<DioceseArvoreResult> resultado = useCase.execute(new EscopoTerritorial.DeDiocese(dioceseId), pageable);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    void shouldThrowWhenScopeIsSetorial() {
        ListarDiocesesUseCase useCase =
                new ListarDiocesesUseCase(dioceseRepository, setorRepository, paroquiaRepository);

        assertThatThrownBy(() -> useCase.execute(new EscopoTerritorial.DeSetor(UUID.randomUUID()), pageable))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenScopeIsParoquia() {
        ListarDiocesesUseCase useCase =
                new ListarDiocesesUseCase(dioceseRepository, setorRepository, paroquiaRepository);

        assertThatThrownBy(() -> useCase.execute(new EscopoTerritorial.DeParoquia(UUID.randomUUID()), pageable))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    /**
     * {@code id} só é atribuído pelo Hibernate na persistência real; em testes de unidade sem
     * banco, a árvore Diocese→Setor→Paróquia precisa de ids não nulos para o agrupamento por
     * {@code Collectors.groupingBy}, daí a atribuição via reflexão.
     */
    private void atribuirId(Object entidade, UUID id) {
        try {
            Field idField = entidade.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entidade, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
