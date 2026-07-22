package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.pessoas.api.dto.PessoaResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResumoResponse;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PessoaResponseMapper {

    @Mapping(target = "paroquiaId", source = "paroquia.id")
    @Mapping(target = "paroquiaNome", source = "paroquia.nome")
    @Mapping(target = "competencias", expression = "java(nomesDeCompetencias(pessoa))")
    @Mapping(target = "indicadoPorId", source = "indicadoPor.id")
    @Mapping(target = "indicadoPorNome", source = "indicadoPor.nomeCompleto")
    PessoaResponse toResponse(Pessoa pessoa);

    @Mapping(target = "paroquiaId", source = "paroquia.id")
    @Mapping(target = "paroquiaNome", source = "paroquia.nome")
    @Mapping(target = "competencias", expression = "java(nomesDeCompetencias(pessoa))")
    @Mapping(target = "numEncontros", constant = "0")
    PessoaResumoResponse toResumoResponse(Pessoa pessoa);

    default List<String> nomesDeCompetencias(Pessoa pessoa) {
        return pessoa.getCompetencias().stream().map(Competencia::getNome).sorted().toList();
    }
}
