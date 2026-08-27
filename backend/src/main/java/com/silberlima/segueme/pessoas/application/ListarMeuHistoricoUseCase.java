package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A linha do tempo da própria pessoa (perfil Seguidor). Somente leitura — o histórico é
 * append-only e nasce dos fluxos do sistema (RN-PESSOA-02), nunca da mão de quem o consulta.
 */
@Service
public class ListarMeuHistoricoUseCase {

    private final PessoaRepository pessoaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public ListarMeuHistoricoUseCase(
            PessoaRepository pessoaRepository, HistoricoEventoRepository historicoEventoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoricoEvento> execute(String usuarioId) {
        return pessoaRepository
                .findByUsuarioIdAndDeletedFalse(usuarioId)
                .map(pessoa -> historicoEventoRepository.findByPessoa_IdOrderByDataEventoAsc(pessoa.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma pessoa do cadastro está vinculada a este usuário."));
    }
}
