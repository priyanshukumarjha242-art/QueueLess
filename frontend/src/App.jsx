import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { WebSocketProvider } from './context/WebSocketContext';
import { Navbar } from './components/Navbar';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { PublicBoardPage } from './pages/PublicBoardPage';
import { CustomerDashboard } from './pages/CustomerDashboard';
import { StaffDashboard } from './pages/StaffDashboard';
import { AdminDashboard } from './pages/AdminDashboard';

// Protected Route Guard
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950">
        <div className="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    // Redirect to their default dashboard
    if (user?.role === 'ROLE_ADMIN') return <Navigate to="/admin" replace />;
    if (user?.role === 'ROLE_STAFF') return <Navigate to="/staff" replace />;
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

// Layout wrapper conditionally omitting standard navbar on public board for full screen
const AppLayout = ({ children }) => {
  const location = useLocation();
  const isBoard = location.pathname === '/board';

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-100 font-sans">
      {!isBoard && <Navbar />}
      <div className="flex-1">
        {children}
      </div>
    </div>
  );
};

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <WebSocketProvider>
          <BrowserRouter>
            <AppLayout>
              <Routes>
                {/* Public Pages */}
                <Route path="/" element={<LandingPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/board" element={<PublicBoardPage />} />

                {/* Customer Dashboard */}
                <Route
                  path="/dashboard"
                  element={
                    <ProtectedRoute allowedRoles={['ROLE_CUSTOMER', 'ROLE_STAFF', 'ROLE_ADMIN']}>
                      <CustomerDashboard />
                    </ProtectedRoute>
                  }
                />

                {/* Staff Dashboard */}
                <Route
                  path="/staff"
                  element={
                    <ProtectedRoute allowedRoles={['ROLE_STAFF', 'ROLE_ADMIN']}>
                      <StaffDashboard />
                    </ProtectedRoute>
                  }
                />

                {/* Administrator Dashboard */}
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                      <AdminDashboard />
                    </ProtectedRoute>
                  }
                />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </AppLayout>
          </BrowserRouter>
        </WebSocketProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}
