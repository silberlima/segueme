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
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
