package com.silberlima.segueme.montagem.api;

import com.silberlima.segueme.montagem.api.dto.CandidatoSugeridoResponse;
import com.silberlima.segueme.montagem.api.dto.EquipeDetalheResponse;
import com.silberlima.segueme.montagem.api.dto.EquipeResumoResponse;
import com.silberlima.segueme.montagem.api.dto.IntegranteResponse;
import com.silberlima.segueme.montagem.api.dto.SubstituicaoResponse;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.MatchScore;
import com.silberlima.segueme.montagem.domain.Substituicao;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MontagemResponseMapper {

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    IntegranteResponse toIntegranteResponse(IntegranteEquipe integrante);

    @Mapping(target = "equipeNome", source = "equipe.nome")
    @Mapping(target = "pessoaSaiId", source = "pessoaSai.id")
    @Mapping(target = "pessoaSaiNome", source = "pessoaSai.nomeCompleto")
    @Mapping(target = "pessoaEntraId", source = "pessoaEntra.id")
    @Mapping(target = "pessoaEntraNome", source = "pessoaEntra.nomeCompleto")
    SubstituicaoResponse toSubstituicaoResponse(Substituicao substituicao);

    default EquipeResumoResponse toResumoResponse(EquipeComposicao composicao) {
        Equipe equipe = composicao.equipe();
        return new EquipeResumoResponse(
                equipe.getId(),
                equipe.getNome(),
                equipe.getMinimo(),
                equipe.getMaximo(),
                composicao.preenchidos(),
                composicao.vagasAbertas(),
                composicao.completa());
    }

    default EquipeDetalheResponse toDetalheResponse(EquipeComposicao composicao) {
        Equipe equipe = composicao.equipe();
        List<String> competencias =
                equipe.getCompetenciasRequeridas().stream().map(Competencia::getNome).sorted().toList();
        List<IntegranteResponse> integrantes =
                composicao.integrantes().stream().map(this::toIntegranteResponse).toList();
        return new EquipeDetalheResponse(
                equipe.getId(),
                equipe.getNome(),
                equipe.getMinimo(),
                equipe.getMaximo(),
                equipe.getPreRequisitoMinEncontros(),
                competencias,
                composicao.preenchidos(),
                composicao.vagasAbertas(),
                composicao.completa(),
                integrantes);
    }

    default CandidatoSugeridoResponse toCandidatoResponse(CandidatoSugerido candidato) {
        Pessoa pessoa = candidato.pessoa();
        MatchScore score = candidato.score();
        int idade = Period.between(pessoa.getDataNascimento(), LocalDate.now()).getYears();
        return new CandidatoSugeridoResponse(
                pessoa.getId(),
                pessoa.getNomeCompleto(),
                idade,
                pessoa.getSituacao().name(),
                pessoa.getParoquia().getNome(),
                candidato.mesmaParoquia(),
                !candidato.mesmaParoquia(),
                score.total(),
                score.versao(),
                new CandidatoSugeridoResponse.ScoreBreakdown(
                        score.historicoEquipesSemelhantes(),
                        score.competencias(),
                        score.frequenciaPosEncontro(),
                        score.preRequisito()),
                candidato.justificativas());
    }
}
