package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.pessoas.api.dto.CriarPessoaRequest;
import com.silberlima.segueme.pessoas.api.dto.HistoricoEventoResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResumoResponse;
import com.silberlima.segueme.pessoas.application.ConsultarPessoaUseCase;
import com.silberlima.segueme.pessoas.application.CriarPessoaUseCase;
import com.silberlima.segueme.pessoas.application.ListarHistoricoPessoaUseCase;
import com.silberlima.segueme.pessoas.application.ListarPessoasUseCase;
import com.silberlima.segueme.pessoas.application.dto.DadosNovaPessoa;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cadastro nacional único de pessoas (RF-007 a RF-011, RN-PESSOA-01 a 05). Leitura liberada aos
 * 10 perfis ("todos os perfis" acessam Pessoas — `cadastro-de-pessoas.md` 2.1); escrita restrita
 * aos perfis de gestão territorial/cadastro — decisão para a pendência #30 de
 * `pendencias-gerais.md`, que não especifica quem pode criar/editar pessoas.
 */
@RestController
@RequestMapping("/api/v1/pessoas")
@Tag(name = "Pessoas", description = "Cadastro nacional único de pessoas (RF-007 a RF-011, RN-PESSOA-01 a 05)")
public class PessoasController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO','SEGUIDOR')";

    private final CriarPessoaUseCase criarPessoaUseCase;
    private final ListarPessoasUseCase listarPessoasUseCase;
    private final ConsultarPessoaUseCase consultarPessoaUseCase;
    private final ListarHistoricoPessoaUseCase listarHistoricoPessoaUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final PessoaResponseMapper pessoaResponseMapper;
    private final HistoricoEventoResponseMapper historicoEventoResponseMapper;

    public PessoasController(
            CriarPessoaUseCase criarPessoaUseCase,
            ListarPessoasUseCase listarPessoasUseCase,
            ConsultarPessoaUseCase consultarPessoaUseCase,
            ListarHistoricoPessoaUseCase listarHistoricoPessoaUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            PessoaResponseMapper pessoaResponseMapper,
            HistoricoEventoResponseMapper historicoEventoResponseMapper) {
        this.criarPessoaUseCase = criarPessoaUseCase;
        this.listarPessoasUseCase = listarPessoasUseCase;
        this.consultarPessoaUseCase = consultarPessoaUseCase;
        this.listarHistoricoPessoaUseCase = listarHistoricoPessoaUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.pessoaResponseMapper = pessoaResponseMapper;
        this.historicoEventoResponseMapper = historicoEventoResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Cadastra uma nova pessoa no cadastro nacional único (RF-009); nasce Pré-inscrito (RN-PESSOA-04)")
    public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody CriarPessoaRequest request) {
        Pessoa pessoa = criarPessoaUseCase.execute(new DadosNovaPessoa(
                request.nomeCompleto(),
                request.dataNascimento(),
                request.telefone(),
                request.estadoCivil(),
                request.paroquiaId(),
                request.alergias(),
                request.restricaoAlimentar(),
                request.medicacaoUsoContinuo(),
                request.contatoEmergenciaNome(),
                request.contatoEmergenciaTelefone(),
                request.contatoEmergenciaParentesco(),
                request.indicadoPorId(),
                request.competenciaIds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(pessoaResponseMapper.toResponse(pessoa));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista pessoas do escopo territorial do usuário, com busca por nome/paróquia/competência (RF-007)")
    public ResponseEntity<PageResponse<PessoaResumoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) UUID paroquiaId,
            @RequestParam(required = false) UUID competenciaId,
            @PageableDefault(sort = "nomeCompleto", direction = Sort.Direction.ASC) Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarPessoasUseCase
                .execute(escopo, nome, paroquiaId, competenciaId, pageable)
                .map(pessoaResponseMapper::toResumoResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Exibe o Perfil Completo de uma pessoa (RF-010)")
    public ResponseEntity<PessoaResponse> consultar(@PathVariable UUID id) {
        Pessoa pessoa = consultarPessoaUseCase.execute(id);
        return ResponseEntity.ok(pessoaResponseMapper.toResponse(pessoa));
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista a linha do tempo de eventos de uma pessoa (RF-011, RN-PESSOA-02)")
    public ResponseEntity<List<HistoricoEventoResponse>> historico(@PathVariable UUID id) {
        List<HistoricoEventoResponse> eventos = listarHistoricoPessoaUseCase.execute(id).stream()
                .map(historicoEventoResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(eventos);
    }

    private EscopoTerritorial resolverEscopo(Jwt jwt, Authentication authentication) {
        List<String> authorities =
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        List<Perfil> perfis = Perfil.fromAuthorities(authorities);
        return escopoTerritorialResolver.resolve(
                perfis,
                jwt.getClaimAsString("diocese_id"),
                jwt.getClaimAsString("setor_id"),
                jwt.getClaimAsString("paroquia_id"));
    }
}
