package com.silberlima.segueme.dashboards.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.RegiaoBrasil;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Cobre os endpoints dos nove painéis (RF-053) de ponta a ponta: cada rota só responde ao perfil
 * mapeado para ela (RN-DASH-01), sem token não responde, e os KPIs vêm de dado real — não mais de
 * valores estáticos como no protótipo.
 */
class DashboardsControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DioceseRepository dioceseRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private ParoquiaRepository paroquiaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EncontroRepository encontroRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/nacional")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenPerfilPedeOPainelDeOutroPerfil() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/nacional")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboards/montagem")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboards/paroquia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldConsolidarOPainelNacionalComDadoReal() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Painel Nacional", RegiaoBrasil.SUL);
        Pessoa seguidor = criarPessoa("Seguidor Nacional", paroquia, SituacaoPessoa.SEGUIDOR);
        criarEncontro(paroquia, seguidor, LocalDate.now());

        mockMvc.perform(get("/api/v1/dashboards/nacional")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seguidores").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.dioceses").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.encontrosPorRegiao.length()").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.diocesesDestaque").isArray());
    }

    @Test
    void shouldConsolidarOPainelDaParoquiaComOEncontroCorrente() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Painel Próprio", RegiaoBrasil.SUDESTE);
        Pessoa diretor = criarPessoa("Diretor Espiritual", paroquia, SituacaoPessoa.SEGUIDOR);
        Encontro encontro = criarEncontro(paroquia, diretor, LocalDate.now().plusDays(10));

        mockMvc.perform(get("/api/v1/dashboards/paroquia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontroCorrente.id").value(encontro.getId().toString()))
                .andExpect(jsonPath("$.encontroCorrente.paroquiaNome").value("Paróquia Painel Próprio"))
                .andExpect(jsonPath("$.encontroCorrente.diretorEspiritualNome").value("Diretor Espiritual"))
                .andExpect(jsonPath("$.diasParaProximoEncontro").value(10))
                .andExpect(jsonPath("$.seguidoresAtivos").value(1))
                .andExpect(jsonPath("$.pendencias.length()").value(3))
                .andExpect(jsonPath("$.financeiro.saldo").value(0));
    }

    @Test
    void shouldReturnPainelDaParoquiaZeradoQuandoNaoHaEncontro() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Sem Encontro", RegiaoBrasil.NORTE);

        mockMvc.perform(get("/api/v1/dashboards/paroquia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontroCorrente").doesNotExist())
                .andExpect(jsonPath("$.totalPendencias").value(0))
                .andExpect(jsonPath("$.encontrosRecentes").isEmpty());
    }

    @Test
    void shouldConsolidarOPainelDaDioceseComSeguidoresPorSetor() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Painel", RegiaoBrasil.NORDESTE));
        Setor setor = setorRepository.save(new Setor("Setor Painel", diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia do Setor Painel", setor));
        criarPessoa("Seguidor do Setor", paroquia, SituacaoPessoa.SEGUIDOR);

        mockMvc.perform(get("/api/v1/dashboards/diocese")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIOCESE"))
                                .jwt(builder -> builder.claim("diocese_id", diocese.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seguidores").value(1))
                .andExpect(jsonPath("$.setores").value(1))
                .andExpect(jsonPath("$.paroquias").value(1))
                .andExpect(jsonPath("$.seguidoresPorSetor[0].nome").value("Setor Painel"))
                .andExpect(jsonPath("$.seguidoresPorSetor[0].total").value(1))
                .andExpect(jsonPath("$.solicitacoesAoSetorial").value(0));
    }

    @Test
    void shouldConsolidarOPainelDoSetorComAsParoquias() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese do Setorial", RegiaoBrasil.SUL));
        Setor setor = setorRepository.save(new Setor("Setor Solicitações", diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia Vizinha", setor));
        Pessoa diretor = criarPessoa("Diretor do Setor", paroquia, SituacaoPessoa.SEGUIDOR);
        criarEncontro(paroquia, diretor, LocalDate.now().plusDays(30));

        mockMvc.perform(get("/api/v1/dashboards/setorial")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitacoesPendentes").value(0))
                .andExpect(jsonPath("$.emprestimosAtivos").value(0))
                .andExpect(jsonPath("$.seguidores").value(1))
                .andExpect(jsonPath("$.paroquias[0].nome").value("Paróquia Vizinha"))
                .andExpect(jsonPath("$.paroquias[0].proximoEncontro").isNotEmpty());
    }

    @Test
    void shouldConsolidarOsPaineisDeEquipeSobreOEncontroCorrente() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia das Equipes", RegiaoBrasil.CENTRO_OESTE);
        Pessoa diretor = criarPessoa("Diretor das Equipes", paroquia, SituacaoPessoa.SEGUIDOR);
        Encontro encontro = criarEncontro(paroquia, diretor, LocalDate.now().plusDays(5));
        String paroquiaId = paroquia.getId().toString();

        mockMvc.perform(get("/api/v1/dashboards/fichas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontroCorrente.id").value(encontro.getId().toString()))
                .andExpect(jsonPath("$.vagasRestantes").value(60))
                .andExpect(jsonPath("$.seguidores").value(1));

        mockMvc.perform(get("/api/v1/dashboards/montagem")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontroCorrente.id").value(encontro.getId().toString()))
                .andExpect(jsonPath("$.equipes").isArray());

        mockMvc.perform(get("/api/v1/dashboards/comando")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convitesEnviados").value(0))
                .andExpect(jsonPath("$.aceitesPorEquipe").isEmpty());

        mockMvc.perform(get("/api/v1/dashboards/financas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.saldo").value(0))
                .andExpect(jsonPath("$.encontroCorrente.numero").value(encontro.getNumero()));

        mockMvc.perform(get("/api/v1/dashboards/pos-encontro")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jovensAcompanhados").value(1))
                .andExpect(jsonPath("$.presencaMedia").value(0))
                .andExpect(jsonPath("$.frequenciaMensal.length()").value(6));
    }

    @Test
    void shouldReturn409WhenUsuarioNaoTemClaimTerritorial() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/paroquia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isConflict());
    }

    private Paroquia criarParoquia(String nome, RegiaoBrasil regiao) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese de " + nome, regiao));
        Setor setor = setorRepository.save(new Setor("Setor de " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Pessoa criarPessoa(String nome, Paroquia paroquia, SituacaoPessoa situacao) {
        Pessoa pessoa = new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 5, 10),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        ReflectionTestUtils.setField(pessoa, "situacao", situacao);
        return pessoaRepository.save(pessoa);
    }

    private Encontro criarEncontro(Paroquia paroquia, Pessoa diretor, LocalDate dataInicio) {
        return encontroRepository.save(new Encontro(
                1,
                "São José",
                diretor,
                "Casa de Retiros",
                dataInicio,
                dataInicio.plusDays(3),
                60,
                paroquia));
    }
}
