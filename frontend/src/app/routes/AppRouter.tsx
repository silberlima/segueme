import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { RootLayout } from './RootLayout'
import { LoginScreen } from '../../domains/sistema/screens/LoginScreen'
import { RecuperarSenhaScreen } from '../../domains/sistema/screens/RecuperarSenhaScreen'
import { NavigationMapScreen } from '../../domains/sistema/screens/NavigationMapScreen'

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
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
