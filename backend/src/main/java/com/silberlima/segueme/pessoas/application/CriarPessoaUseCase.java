package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.pessoas.application.dto.DadosNovaPessoa;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra uma nova Pessoa no cadastro nacional único (RF-009). Nasce com situação
 * {@code PRE_INSCRITO} (RN-PESSOA-04, valor padrão do campo em {@link Pessoa}) e recebe o
 * primeiro evento do seu histórico (RN-PESSOA-02).
 */
@Service
public class CriarPessoaUseCase {

    private final PessoaRepository pessoaRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public CriarPessoaUseCase(
            PessoaRepository pessoaRepository,
            ParoquiaRepository paroquiaRepository,
            CompetenciaRepository competenciaRepository,
            HistoricoEventoRepository historicoEventoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.competenciaRepository = competenciaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional
    public Pessoa execute(DadosNovaPessoa dados) {
        Paroquia paroquia = paroquiaRepository
                .findByIdAndDeletedFalse(dados.paroquiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Paróquia não encontrada: " + dados.paroquiaId()));

        Pessoa indicadoPor = null;
        if (dados.indicadoPorId() != null) {
            indicadoPor = pessoaRepository
                    .findByIdAndDeletedFalse(dados.indicadoPorId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Pessoa indicadora não encontrada: " + dados.indicadoPorId()));
        }

        Pessoa pessoa = new Pessoa(
                dados.nomeCompleto(),
                dados.dataNascimento(),
                dados.telefone(),
                dados.estadoCivil(),
                paroquia,
                dados.alergias(),
                dados.restricaoAlimentar(),
                dados.medicacaoUsoContinuo(),
                dados.contatoEmergenciaNome(),
                dados.contatoEmergenciaTelefone(),
                dados.contatoEmergenciaParentesco(),
                indicadoPor);

        if (!dados.competenciaIds().isEmpty()) {
            Set<Competencia> competencias = new HashSet<>(competenciaRepository.findAllById(dados.competenciaIds()));
            pessoa.adicionarCompetencias(competencias);
        }

        Pessoa salva = pessoaRepository.save(pessoa);

        historicoEventoRepository.save(new HistoricoEvento(
                salva,
                TipoEventoHistorico.INSCRICAO_CADASTRO_NACIONAL,
                Instant.now(),
                "Pessoa cadastrada no cadastro nacional único."));

        return salva;
    }
}
