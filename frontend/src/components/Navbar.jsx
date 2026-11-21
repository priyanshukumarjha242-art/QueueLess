import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useWebSocket } from '../context/WebSocketContext';
import { Sun, Moon, LogOut, Layers, Radio, Monitor, User, ShieldCheck, UserCheck } from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, logout, isCustomer, isStaff, isAdmin } = useAuth();
  const { darkMode, toggleTheme } = useTheme();
  const { connected } = useWebSocket();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardPath = () => {
    if (isAdmin) return '/admin';
    if (isStaff) return '/staff';
    return '/dashboard';
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <div className="flex items-center gap-6">
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-sky-400 flex items-center justify-center text-white shadow-md shadow-brand-500/20 group-hover:scale-105 transition-transform">
              <Layers className="w-5 h-5" />
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-slate-900 via-brand-700 to-sky-600 dark:from-white dark:via-slate-100 dark:to-sky-400 bg-clip-text text-transparent">
                QueueLess
              </span>
              <span className="text-[10px] uppercase tracking-wider font-semibold text-slate-400 dark:text-slate-500 -mt-1">
                Real-Time Ops
              </span>
            </div>
          </Link>

          {/* WebSocket indicator */}
          <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300">
            <span className={`w-2 h-2 rounded-full ${connected ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
            <span>{connected ? 'Live Sync Active' : 'Connecting...'}</span>
          </div>
        </div>

        {/* Right Nav */}
        <div className="flex items-center gap-3">
          {/* Public Board Link */}
          <Link
            to="/board"
            className="hidden md:flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <Monitor className="w-4 h-4 text-brand-600 dark:text-brand-400" />
            <span>Public Waiting Board</span>
          </Link>

          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            aria-label="Toggle Theme"
            className="p-2 rounded-lg text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            {darkMode ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4" />}
          </button>

          {/* User Section */}
          {isAuthenticated ? (
            <div className="flex items-center gap-3 pl-2 border-l border-slate-200 dark:border-slate-800">
              <Link
                to={getDashboardPath()}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 transition"
              >
                {isAdmin ? (
                  <ShieldCheck className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400" />
                ) : isStaff ? (
                  <UserCheck className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                ) : (
                  <User className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
                )}
                <span className="font-semibold text-slate-800 dark:text-slate-200 truncate max-w-[120px]">
                  {user?.fullName?.split(' ')[0]}
                </span>
                <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-brand-100 text-brand-800 dark:bg-brand-950 dark:text-brand-300">
                  {user?.role?.replace('ROLE_', '')}
                </span>
              </Link>

              <button
                onClick={handleLogout}
                title="Log Out"
                className="p-2 text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 rounded-lg transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-3.5 py-1.5 text-xs font-semibold rounded-lg text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="px-3.5 py-1.5 text-xs font-semibold rounded-lg text-white bg-brand-600 hover:bg-brand-700 shadow-sm shadow-brand-500/30 transition"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
