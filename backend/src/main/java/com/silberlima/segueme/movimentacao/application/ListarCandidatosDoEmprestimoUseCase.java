package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os candidatos já selecionados (sugeridos ou efetivados) de um {@link
 * com.silberlima.segueme.movimentacao.domain.Emprestimo} — usado para compor a resposta de API
 * com o sub-recurso {@code candidatos} embutido, já que não há coleção bidirecional no agregado
 * (`skills/05-persistencia.md`: nenhum {@code @OneToMany} no domínio, apenas {@code @ManyToOne}).
 */
@Service
public class ListarCandidatosDoEmprestimoUseCase {

    private final EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    public ListarCandidatosDoEmprestimoUseCase(EmprestimoCandidatoRepository emprestimoCandidatoRepository) {
        this.emprestimoCandidatoRepository = emprestimoCandidatoRepository;
    }

    @Transactional(readOnly = true)
    public List<EmprestimoCandidato> execute(UUID emprestimoId) {
        return emprestimoCandidatoRepository.findByEmprestimo_Id(emprestimoId);
    }
}
