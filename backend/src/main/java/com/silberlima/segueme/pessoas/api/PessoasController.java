package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.pessoas.api.dto.AtualizarMeusDadosRequest;
import com.silberlima.segueme.pessoas.api.dto.CriarPessoaRequest;
import com.silberlima.segueme.pessoas.api.dto.HistoricoEventoResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResumoResponse;
import com.silberlima.segueme.pessoas.application.AtualizarMeusDadosUseCase;
import com.silberlima.segueme.pessoas.application.ConsultarMeusDadosUseCase;
import com.silberlima.segueme.pessoas.application.ConsultarPessoaUseCase;
import com.silberlima.segueme.pessoas.application.CriarPessoaUseCase;
import com.silberlima.segueme.pessoas.application.ListarHistoricoPessoaUseCase;
import com.silberlima.segueme.pessoas.application.ListarMeuHistoricoUseCase;
import com.silberlima.segueme.pessoas.application.ListarPessoasUseCase;
import com.silberlima.segueme.pessoas.application.dto.DadosNovaPessoa;
import com.silberlima.segueme.pessoas.application.dto.DadosPessoaisEditaveis;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cadastro nacional único de pessoas (RF-007 a RF-011, RN-PESSOA-01 a 05).
 *
 * <p>O protótipo dizia "todos os perfis" acessam Pessoas e registrava que a permissão granular não
 * estava detalhada (`cadastro-de-pessoas.md` 2.1). Definido depois, com o PO:
 *
 * <ul>
 *   <li><b>Nacional fica de fora</b> do cadastro de pessoas: cuida de dioceses, setores e
 *       paróquias. Gente é assunto de quem convive com ela — paróquia, setorial e equipes.
 *   <li><b>Seguidor não enxerga o cadastro dos outros</b>, só o próprio, pelos endpoints
 *       {@code /me} abaixo. Como todo usuário do sistema é também um seguidor, esses endpoints
 *       valem para qualquer perfil autenticado.
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/pessoas")
@Tag(name = "Pessoas", description = "Cadastro nacional único de pessoas (RF-007 a RF-011, RN-PESSOA-01 a 05)")
public class PessoasController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO')";
    /** "Meus dados" não pede perfil: todo usuário autenticado é um seguidor e tem os seus. */
    private static final String QUALQUER_AUTENTICADO = "isAuthenticated()";

    private final CriarPessoaUseCase criarPessoaUseCase;
    private final ListarPessoasUseCase listarPessoasUseCase;
    private final ConsultarPessoaUseCase consultarPessoaUseCase;
    private final ListarHistoricoPessoaUseCase listarHistoricoPessoaUseCase;
    private final ConsultarMeusDadosUseCase consultarMeusDadosUseCase;
    private final AtualizarMeusDadosUseCase atualizarMeusDadosUseCase;
    private final ListarMeuHistoricoUseCase listarMeuHistoricoUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final PessoaResponseMapper pessoaResponseMapper;
    private final HistoricoEventoResponseMapper historicoEventoResponseMapper;

    public PessoasController(
            CriarPessoaUseCase criarPessoaUseCase,
            ListarPessoasUseCase listarPessoasUseCase,
            ConsultarPessoaUseCase consultarPessoaUseCase,
            ListarHistoricoPessoaUseCase listarHistoricoPessoaUseCase,
            ConsultarMeusDadosUseCase consultarMeusDadosUseCase,
            AtualizarMeusDadosUseCase atualizarMeusDadosUseCase,
            ListarMeuHistoricoUseCase listarMeuHistoricoUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            PessoaResponseMapper pessoaResponseMapper,
            HistoricoEventoResponseMapper historicoEventoResponseMapper) {
        this.criarPessoaUseCase = criarPessoaUseCase;
        this.listarPessoasUseCase = listarPessoasUseCase;
        this.consultarPessoaUseCase = consultarPessoaUseCase;
        this.listarHistoricoPessoaUseCase = listarHistoricoPessoaUseCase;
        this.consultarMeusDadosUseCase = consultarMeusDadosUseCase;
        this.atualizarMeusDadosUseCase = atualizarMeusDadosUseCase;
        this.listarMeuHistoricoUseCase = listarMeuHistoricoUseCase;
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

    /*
     * Os três endpoints abaixo vêm antes de /{id} de propósito: o Spring casa padrão literal antes
     * de template, então "me" nunca é confundido com um id.
     */

    @GetMapping("/me")
    @PreAuthorize(QUALQUER_AUTENTICADO)
    @Operation(summary = "Exibe os dados da pessoa que o usuário autenticado é")
    public ResponseEntity<PessoaResponse> meusDados(@AuthenticationPrincipal Jwt jwt) {
        Pessoa pessoa = consultarMeusDadosUseCase.execute(jwt.getSubject());
        return ResponseEntity.ok(pessoaResponseMapper.toResponse(pessoa));
    }

    @PutMapping("/me")
    @PreAuthorize(QUALQUER_AUTENTICADO)
    @Operation(summary = "Altera os dados de contato e saúde da própria pessoa")
    public ResponseEntity<PessoaResponse> atualizarMeusDados(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AtualizarMeusDadosRequest request) {
        Pessoa pessoa = atualizarMeusDadosUseCase.execute(
                jwt.getSubject(),
                new DadosPessoaisEditaveis(
                        request.telefone(),
                        request.alergias(),
                        request.restricaoAlimentar(),
                        request.medicacaoUsoContinuo(),
                        request.contatoEmergenciaNome(),
                        request.contatoEmergenciaTelefone(),
                        request.contatoEmergenciaParentesco()));
        return ResponseEntity.ok(pessoaResponseMapper.toResponse(pessoa));
    }

    @GetMapping("/me/historico")
    @PreAuthorize(QUALQUER_AUTENTICADO)
    @Operation(summary = "Linha do tempo da própria pessoa, somente leitura (RN-PESSOA-02)")
    public ResponseEntity<List<HistoricoEventoResponse>> meuHistorico(@AuthenticationPrincipal Jwt jwt) {
        List<HistoricoEventoResponse> eventos = listarMeuHistoricoUseCase.execute(jwt.getSubject()).stream()
                .map(historicoEventoResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(eventos);
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
