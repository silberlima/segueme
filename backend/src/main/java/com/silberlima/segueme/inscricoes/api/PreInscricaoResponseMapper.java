package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.inscricoes.api.dto.FichaInscricaoResponse;
import com.silberlima.segueme.inscricoes.api.dto.PreInscricaoResponse;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PreInscricaoResponseMapper {

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "idade", expression = "java(idade(preInscricao.getPessoa()))")
    @Mapping(target = "indicadoPorNome", expression = "java(nomeIndicadoPor(preInscricao.getPessoa()))")
    @Mapping(target = "encontroId", source = "encontro.id")
    @Mapping(target = "recebidaEm", source = "createdAt")
    PreInscricaoResponse toResponse(PreInscricao preInscricao);

    @Mapping(target = "preInscricaoId", source = "preInscricao.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "encontroId", source = "encontro.id")
    FichaInscricaoResponse toResponse(FichaInscricao ficha);

    default int idade(Pessoa pessoa) {
        return Period.between(pessoa.getDataNascimento(), LocalDate.now()).getYears();
    }

    default String nomeIndicadoPor(Pessoa pessoa) {
        Pessoa indicadoPor = pessoa.getIndicadoPor();
        return indicadoPor == null ? null : indicadoPor.getNomeCompleto();
    }
}
