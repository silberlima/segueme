package com.silberlima.segueme.conteudo.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.conteudo.api.dto.CadastrarPalestranteRequest;
import com.silberlima.segueme.conteudo.api.dto.PalestranteResponse;
import com.silberlima.segueme.conteudo.application.CadastrarPalestranteUseCase;
import com.silberlima.segueme.conteudo.application.ListarPalestrantesUseCase;
import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro de palestrantes nos 3 tipos previstos (RF-052, `screen_palestrantes`). */
@RestController
@RequestMapping("/api/v1/palestrantes")
@Tag(name = "Conteúdo — Palestrantes", description = "Cadastro de palestrantes: convidado externo, seguidor ou clero (RF-052)")
public class PalestrantesController {

    private static final String PERFIS_CADASTRO = "hasAnyRole('PAROQUIA','EQUIPE_COMANDO')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO','SEGUIDOR')";

    private final CadastrarPalestranteUseCase cadastrarPalestranteUseCase;
    private final ListarPalestrantesUseCase listarPalestrantesUseCase;
    private final PalestranteResponseMapper palestranteResponseMapper;

    public PalestrantesController(
            CadastrarPalestranteUseCase cadastrarPalestranteUseCase,
            ListarPalestrantesUseCase listarPalestrantesUseCase,
            PalestranteResponseMapper palestranteResponseMapper) {
        this.cadastrarPalestranteUseCase = cadastrarPalestranteUseCase;
        this.listarPalestrantesUseCase = listarPalestrantesUseCase;
        this.palestranteResponseMapper = palestranteResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_CADASTRO)
    @Operation(summary = "Cadastra um palestrante — convidado externo, seguidor ou clero (RN-CONTEUDO-02)")
    public ResponseEntity<PalestranteResponse> cadastrar(@Valid @RequestBody CadastrarPalestranteRequest request) {
        var palestrante = cadastrarPalestranteUseCase.execute(request.tipo(), request.nome(), request.pessoaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(palestranteResponseMapper.toResponse(palestrante));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista os palestrantes cadastrados, com filtro opcional por tipo")
    public ResponseEntity<PageResponse<PalestranteResponse>> listar(
            @RequestParam(required = false) TipoPalestrante tipo, @PageableDefault Pageable pageable) {
        var pagina = listarPalestrantesUseCase.execute(tipo, pageable).map(palestranteResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }
}
