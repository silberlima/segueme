package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Os cartões de tipo de relatório que a tela deve exibir ao usuário (RF-056). Vem do backend para
 * que a lista de tipos e a regra de quem pode gerar cada um existam num lugar só.
 */
@Service
public class ListarTiposDeRelatorioUseCase {

    public List<TipoRelatorio> execute(List<Perfil> perfis) {
        return TipoRelatorio.disponiveisPara(perfis);
    }
}
