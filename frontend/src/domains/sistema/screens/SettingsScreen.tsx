import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, PageHeader, PanelCard, Skeleton, Toggle } from '../../../shared/ui'
import { DESCRICAO_POR_TIPO, LABEL_POR_TIPO } from '../domain/notificacao.entity'
import { PERFIL_LABEL } from '../domain/perfil.types'
import { useMe } from '../hooks/useMe'
import { useNotificationPreferencesForm } from '../hooks/useNotificationPreferencesForm'

/**
 * Configurações (`screen_config`, RF-057) com as três seções do protótipo: Conta, Notificações e
 * Perfis de acesso.
 *
 * <p>Conta e Perfis de acesso são de leitura: quem atribui perfil a um usuário real é a pendência
 * #3 de `pendencias-gerais.md`, e os perfis vêm hoje das roles do Keycloak. Editar aqui daria a
 * impressão de um poder que o sistema não tem.
 */
export function SettingsScreen() {
  const { data: me, isLoading: isLoadingMe } = useMe()
  const { perfilAtivo } = useProfile()
  const { preferencias, isLoading, isError, alternar, salvar, isSalvando, mostrarConfirmacao, podeSalvar } =
    useNotificationPreferencesForm()

  return (
    <div className="p-6">
      <PageHeader titulo="Configurações" subtitulo="Preferências da sua conta" />

      <div className="flex max-w-3xl flex-col gap-6">
        <PanelCard titulo="Conta">
          {isLoadingMe ? (
            <Skeleton variant="rect" height="4rem" />
          ) : (
            <dl className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
              <div>
                <dt className="text-xs text-slate-500">Nome</dt>
                <dd className="font-medium text-slate-900">{me?.nome ?? '—'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-500">E-mail</dt>
                <dd className="font-medium text-slate-900">{me?.email ?? '—'}</dd>
              </div>
            </dl>
          )}
        </PanelCard>

        <PanelCard
          titulo="Notificações"
          acao={
            <div className="flex items-center gap-3">
              {mostrarConfirmacao && <span className="text-xs text-emerald-600">Preferências salvas.</span>}
              <Button disabled={!podeSalvar} onClick={salvar}>
                {isSalvando ? 'Salvando...' : 'Salvar'}
              </Button>
            </div>
          }
        >
          {isLoading && <Skeleton variant="rect" height="8rem" />}
          {isError && <p className="text-sm text-rose-600">Não foi possível carregar as preferências.</p>}
          {!isLoading && !isError && (
            <div className="divide-y divide-slate-100">
              {preferencias.map((preferencia) => (
                <Toggle
                  key={preferencia.tipo}
                  label={LABEL_POR_TIPO[preferencia.tipo]}
                  descricao={DESCRICAO_POR_TIPO[preferencia.tipo]}
                  checked={preferencia.ativo}
                  disabled={isSalvando}
                  onChange={(ativo) => alternar(preferencia.tipo, ativo)}
                />
              ))}
            </div>
          )}
        </PanelCard>

        <PanelCard titulo="Perfis de acesso">
          <p className="mb-3 text-xs text-slate-500">
            Perfis concedidos à sua conta. A troca do perfil ativo é feita no menu lateral
            (RN-ACESSO-02); a atribuição de perfis é administrada fora do sistema.
          </p>
          {isLoadingMe ? (
            <Skeleton variant="rect" height="3rem" />
          ) : (
            <ul className="flex flex-wrap gap-2">
              {(me?.perfis ?? []).map((perfil) => (
                <li key={perfil}>
                  <Badge tone={perfil === perfilAtivo ? 'info' : 'neutral'}>
                    {PERFIL_LABEL[perfil]}
                    {perfil === perfilAtivo ? ' · ativo' : ''}
                  </Badge>
                </li>
              ))}
            </ul>
          )}
        </PanelCard>
      </div>
    </div>
  )
}
