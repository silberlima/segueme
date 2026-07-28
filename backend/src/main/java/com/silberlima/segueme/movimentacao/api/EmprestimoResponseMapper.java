package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.movimentacao.api.dto.CandidatoElegivelResponse;
import com.silberlima.segueme.movimentacao.api.dto.EmprestimoCandidatoResponse;
import com.silberlima.segueme.movimentacao.api.dto.EmprestimoResponse;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmprestimoResponseMapper {

    @Mapping(target = "encontroId", source = "emprestimo.encontro.id")
    @Mapping(target = "encontroNumero", source = "emprestimo.encontro.numero")
    @Mapping(target = "equipeId", source = "emprestimo.equipe.id")
    @Mapping(target = "equipeNome", source = "emprestimo.equipe.nome")
    @Mapping(target = "paroquiaSolicitanteNome", source = "emprestimo.encontro.paroquia.nome")
    @Mapping(target = "competenciaRequeridaId", source = "emprestimo.competenciaRequerida.id")
    @Mapping(target = "competenciaRequeridaNome", source = "emprestimo.competenciaRequerida.nome")
    @Mapping(target = "candidatos", source = "candidatos")
    EmprestimoResponse toResponse(Emprestimo emprestimo, List<EmprestimoCandidato> candidatos);

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "paroquiaOrigemId", source = "pessoa.paroquia.id")
    @Mapping(target = "paroquiaOrigemNome", source = "pessoa.paroquia.nome")
    EmprestimoCandidatoResponse toCandidatoResponse(EmprestimoCandidato candidato);

    default CandidatoElegivelResponse toElegivelResponse(Pessoa pessoa) {
        List<String> competencias = pessoa.getCompetencias().stream().map(Competencia::getNome).sorted().toList();
        return new CandidatoElegivelResponse(
                pessoa.getId(), pessoa.getNomeCompleto(), pessoa.getParoquia().getId(), pessoa.getParoquia().getNome(), competencias);
    }
}
