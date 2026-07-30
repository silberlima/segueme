package com.silberlima.segueme.relatorios.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Um relatório solicitado por um usuário (RF-056, UC-22): que tipo, sobre que escopo, e o que a
 * geração produziu. Fica em "Gerados recentemente" na tela Relatórios.
 *
 * <p>O conteúdo é guardado na própria linha porque o MVP gera CSV de poucos milhares de linhas e o
 * projeto ainda não tem armazenamento de arquivos; quando o formato for decidido (pendência #37),
 * este é o campo que vira referência para o arquivo.
 */
@Entity
@Table(name = "relatorio")
public class Relatorio extends AbstractAuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoRelatorio tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusRelatorio status = StatusRelatorio.PENDENTE;

    @Column(name = "solicitante_id", nullable = false)
    private String solicitanteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "escopo_nivel", nullable = false, length = 20)
    private NivelDoEscopo escopoNivel;

    @Column(name = "escopo_id")
    private UUID escopoId;

    @Column(name = "gerado_em")
    private Instant geradoEm;

    @Column(name = "total_linhas")
    private Integer totalLinhas;

    @Column(name = "conteudo", columnDefinition = "text")
    private String conteudo;

    @Column(name = "erro")
    private String erro;

    protected Relatorio() {
    }

    public Relatorio(TipoRelatorio tipo, String solicitanteId, EscopoTerritorial escopo) {
        this.tipo = tipo;
        this.solicitanteId = solicitanteId;
        switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> definirEscopo(NivelDoEscopo.NACIONAL, null);
            case EscopoTerritorial.DeDiocese deDiocese ->
                    definirEscopo(NivelDoEscopo.DIOCESE, deDiocese.dioceseId());
            case EscopoTerritorial.DeSetor deSetor -> definirEscopo(NivelDoEscopo.SETOR, deSetor.setorId());
            case EscopoTerritorial.DeParoquia deParoquia ->
                    definirEscopo(NivelDoEscopo.PAROQUIA, deParoquia.paroquiaId());
        }
    }

    private void definirEscopo(NivelDoEscopo nivel, UUID territorioId) {
        this.escopoNivel = nivel;
        this.escopoId = territorioId;
    }

    public EscopoTerritorial escopo() {
        return switch (escopoNivel) {
            case NACIONAL -> new EscopoTerritorial.Nacional();
            case DIOCESE -> new EscopoTerritorial.DeDiocese(escopoId);
            case SETOR -> new EscopoTerritorial.DeSetor(escopoId);
            case PAROQUIA -> new EscopoTerritorial.DeParoquia(escopoId);
        };
    }

    public void concluir(ConteudoRelatorio conteudoGerado) {
        this.status = StatusRelatorio.CONCLUIDO;
        this.conteudo = conteudoGerado.paraCsv();
        this.totalLinhas = conteudoGerado.totalLinhas();
        this.geradoEm = Instant.now();
        this.erro = null;
    }

    public void falhar(String motivo) {
        this.status = StatusRelatorio.FALHOU;
        this.erro = motivo;
        this.geradoEm = Instant.now();
    }

    public boolean pertenceA(String usuarioId) {
        return solicitanteId.equals(usuarioId);
    }

    public TipoRelatorio getTipo() {
        return tipo;
    }

    public StatusRelatorio getStatus() {
        return status;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public NivelDoEscopo getEscopoNivel() {
        return escopoNivel;
    }

    public UUID getEscopoId() {
        return escopoId;
    }

    public Instant getGeradoEm() {
        return geradoEm;
    }

    public Integer getTotalLinhas() {
        return totalLinhas;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getErro() {
        return erro;
    }
}
