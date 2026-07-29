import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { RootLayout } from './RootLayout'
import { LoginScreen } from '../../domains/sistema/screens/LoginScreen'
import { RecuperarSenhaScreen } from '../../domains/sistema/screens/RecuperarSenhaScreen'
import { NavigationMapScreen } from '../../domains/sistema/screens/NavigationMapScreen'
import { DiocesesScreen } from '../../domains/hierarquia/screens/DiocesesScreen'
import { ParoquiasScreen } from '../../domains/hierarquia/screens/ParoquiasScreen'
import { PersonListScreen } from '../../domains/pessoas/screens/PersonListScreen'
import { PersonFormScreen } from '../../domains/pessoas/screens/PersonFormScreen'
import { PersonProfileScreen } from '../../domains/pessoas/screens/PersonProfileScreen'
import { PersonHistoryScreen } from '../../domains/pessoas/screens/PersonHistoryScreen'
import { CouplesScreen } from '../../domains/pessoas/screens/CouplesScreen'
import { CompetenciesScreen } from '../../domains/pessoas/screens/CompetenciesScreen'
import { EncounterListScreen } from '../../domains/encontros/screens/EncounterListScreen'
import { CreateEncounterScreen } from '../../domains/encontros/screens/CreateEncounterScreen'
import { EncounterDetailScreen } from '../../domains/encontros/screens/EncounterDetailScreen'
import { DirigenteScreen } from '../../domains/encontros/screens/DirigenteScreen'
import { ComandoTeamScreen } from '../../domains/encontros/screens/ComandoTeamScreen'
import { PreEnrollmentScreen } from '../../domains/inscricoes/screens/PreEnrollmentScreen'
import { EnrollmentFormScreen } from '../../domains/inscricoes/screens/EnrollmentFormScreen'
import { WaitlistScreen } from '../../domains/inscricoes/screens/WaitlistScreen'
import { ParticipantsScreen } from '../../domains/inscricoes/screens/ParticipantsScreen'
import { TeamsWorkspaceScreen } from '../../domains/montagem/screens/TeamsWorkspaceScreen'
import { SuggestionsScreen } from '../../domains/montagem/screens/SuggestionsScreen'
import { SubstitutionsScreen } from '../../domains/montagem/screens/SubstitutionsScreen'
import { InvitesScreen } from '../../domains/montagem/screens/InvitesScreen'
import { AcceptancesScreen } from '../../domains/montagem/screens/AcceptancesScreen'
import { RefusalsScreen } from '../../domains/montagem/screens/RefusalsScreen'
import { CoordinatorPanelScreen } from '../../domains/montagem/screens/CoordinatorPanelScreen'
import { TransfersScreen } from '../../domains/setorial/screens/TransfersScreen'
import { LoansScreen } from '../../domains/setorial/screens/LoansScreen'
import { RequestsInboxScreen } from '../../domains/setorial/screens/RequestsInboxScreen'
import { FinanceOverviewScreen } from '../../domains/financeiro/screens/FinanceOverviewScreen'
import { RevenuesScreen } from '../../domains/financeiro/screens/RevenuesScreen'
import { ExpensesScreen } from '../../domains/financeiro/screens/ExpensesScreen'
import { CampaignsScreen } from '../../domains/financeiro/screens/CampaignsScreen'
import { LecturesScreen } from '../../domains/conteudo/screens/LecturesScreen'
import { SpeakersScreen } from '../../domains/conteudo/screens/SpeakersScreen'

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginScreen />} />
        <Route path="/recuperar-senha" element={<RecuperarSenhaScreen />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <RootLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/mapa" replace />} />
          <Route path="mapa" element={<NavigationMapScreen />} />
          <Route path="hierarquia/dioceses" element={<DiocesesScreen />} />
          <Route path="hierarquia/paroquias" element={<ParoquiasScreen />} />
          <Route path="pessoas" element={<PersonListScreen />} />
          <Route path="pessoas/novo" element={<PersonFormScreen />} />
          <Route path="pessoas/casais" element={<CouplesScreen />} />
          <Route path="pessoas/competencias" element={<CompetenciesScreen />} />
          <Route path="pessoas/:id" element={<PersonProfileScreen />} />
          <Route path="pessoas/:id/historico" element={<PersonHistoryScreen />} />
          <Route path="encontros" element={<EncounterListScreen />} />
          <Route path="encontros/novo" element={<CreateEncounterScreen />} />
          <Route path="encontros/:id" element={<EncounterDetailScreen />} />
          <Route path="encontros/:id/dirigente" element={<DirigenteScreen />} />
          <Route path="encontros/:id/comando" element={<ComandoTeamScreen />} />
          <Route path="encontros/:id/inscricoes" element={<PreEnrollmentScreen />} />
          <Route path="encontros/:id/inscricoes/:preInscricaoId/ficha" element={<EnrollmentFormScreen />} />
          <Route path="encontros/:id/reserva" element={<WaitlistScreen />} />
          <Route path="encontros/:id/participantes" element={<ParticipantsScreen />} />
          <Route path="encontros/:id/equipes" element={<TeamsWorkspaceScreen />} />
          <Route path="encontros/:id/equipes/:equipeId/sugestoes" element={<SuggestionsScreen />} />
          <Route path="encontros/:id/substituicoes" element={<SubstitutionsScreen />} />
          <Route path="encontros/:id/convites" element={<InvitesScreen />} />
          <Route path="encontros/:id/aceites" element={<AcceptancesScreen />} />
          <Route path="encontros/:id/recusas" element={<RefusalsScreen />} />
          <Route path="encontros/:id/equipes/:equipeId/coordenador" element={<CoordinatorPanelScreen />} />
          <Route path="setorial/transferencias" element={<TransfersScreen />} />
          <Route path="setorial/emprestimos" element={<LoansScreen />} />
          <Route path="setorial/solicitacoes" element={<RequestsInboxScreen />} />
          <Route path="encontros/:id/financeiro" element={<FinanceOverviewScreen />} />
          <Route path="encontros/:id/financeiro/receitas" element={<RevenuesScreen />} />
          <Route path="encontros/:id/financeiro/despesas" element={<ExpensesScreen />} />
          <Route path="encontros/:id/financeiro/campanhas" element={<CampaignsScreen />} />
          <Route path="encontros/:id/palestras" element={<LecturesScreen />} />
          <Route path="encontros/:id/palestrantes" element={<SpeakersScreen />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
