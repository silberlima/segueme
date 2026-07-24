package com.silberlima.segueme.encontros.api;

import com.silberlima.segueme.encontros.api.dto.EncontroResponse;
import com.silberlima.segueme.encontros.api.dto.EncontroResumoResponse;
import com.silberlima.segueme.encontros.api.dto.EquipeComandoResponse;
import com.silberlima.segueme.encontros.api.dto.MembroDirigenteResponse;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EquipeComando;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigente;
import com.silberlima.segueme.encontros.domain.StatusEncontro;
import com.silberlima.segueme.pessoas.domain.Casal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EncontroResponseMapper {

    @Mapping(target = "id", source = "encontro.id")
    @Mapping(target = "createdAt", source = "encontro.createdAt")
    @Mapping(target = "paroquiaId", source = "encontro.paroquia.id")
    @Mapping(target = "paroquiaNome", source = "encontro.paroquia.nome")
    @Mapping(target = "diretorEspiritualId", source = "encontro.diretorEspiritual.id")
    @Mapping(target = "diretorEspiritualNome", source = "encontro.diretorEspiritual.nomeCompleto")
    @Mapping(target = "equipeDirigente", source = "membrosDirigente")
    @Mapping(target = "equipeComando", source = "equipeComando")
    EncontroResponse toResponse(Encontro encontro, List<MembroEquipeDirigente> membrosDirigente, EquipeComando equipeComando);

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "casalId", source = "casal.id")
    @Mapping(target = "casalNome", expression = "java(nomeCasal(membro.getCasal()))")
    MembroDirigenteResponse toMembroResponse(MembroEquipeDirigente membro);

    @Mapping(target = "coordenadorGeralId", source = "coordenadorGeral.id")
    @Mapping(target = "coordenadorGeralNome", source = "coordenadorGeral.nomeCompleto")
    @Mapping(target = "casalCoordenadorId", source = "casalCoordenador.id")
    @Mapping(target = "casalCoordenadorNome", expression = "java(nomeCasal(equipeComando.getCasalCoordenador()))")
    EquipeComandoResponse toEquipeComandoResponse(EquipeComando equipeComando);

    @Mapping(target = "paroquiaId", source = "paroquia.id")
    @Mapping(target = "paroquiaNome", source = "paroquia.nome")
    @Mapping(target = "coordenadorGeralNome", expression = "java(nomeCoordenadorGeral(encontro))")
    @Mapping(target = "faseAtual", expression = "java(faseAtual(encontro))")
    @Mapping(target = "numSeguidores", constant = "0")
    @Mapping(target = "numEquipes", constant = "0")
    @Mapping(target = "numEquipistas", constant = "0")
    EncontroResumoResponse toResumoResponse(Encontro encontro);

    default String nomeCasal(Casal casal) {
        if (casal == null) {
            return null;
        }
        return casal.getPessoa1().getNomeCompleto() + " & " + casal.getPessoa2().getNomeCompleto();
    }

    default String nomeCoordenadorGeral(Encontro encontro) {
        EquipeComando equipeComando = encontro.getEquipeComando();
        return equipeComando == null ? null : equipeComando.getCoordenadorGeral().getNomeCompleto();
    }

    /** Placeholder até as Fases 5/6/11 existirem — ver {@link EncontroResumoResponse}. */
    default String faseAtual(Encontro encontro) {
        return encontro.getStatus() == StatusEncontro.ENCERRADO ? "Encerrado" : "Aguardando Equipe Comando";
    }
}
