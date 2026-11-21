import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';
import {
  Layers, Users, Clock, ShieldCheck, ArrowRight, CheckCircle2,
  Building2, Hospital, GraduationCap, Landmark, Sparkles, Monitor,
  Activity, ArrowUpRight, ChevronRight, Zap
} from 'lucide-react';

export const LandingPage = () => {
  const { login, isAuthenticated, isAdmin, isStaff } = useAuth();
  const navigate = useNavigate();
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [demoLoading, setDemoLoading] = useState(false);

  useEffect(() => {
    const fetchOrgs = async () => {
      try {
        const res = await api.get('/public/organizations');
        setOrganizations(res.data);
      } catch (err) {
        console.error('Failed to load organizations', err);
      } finally {
        setLoading(false);
      }
    };
    fetchOrgs();
  }, []);

  const handleQuickDemoLogin = async (email, password, redirectPath) => {
    try {
      setDemoLoading(true);
      await login(email, password);
      navigate(redirectPath);
    } catch (err) {
      alert('Demo login failed. Make sure the backend server is running.');
    } finally {
      setDemoLoading(false);
    }
  };

  const getOrgIcon = (type) => {
    switch (type) {
      case 'HOSPITAL': return Hospital;
      case 'COLLEGE': return GraduationCap;
      case 'BANK': return Landmark;
      default: return Building2;
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 selection:bg-brand-500 selection:text-white">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28 border-b border-slate-200 dark:border-slate-800 bg-gradient-to-b from-white via-slate-50 to-slate-100 dark:from-slate-900 dark:via-slate-950 dark:to-slate-950">
        <div className="absolute inset-0 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:24px_24px] opacity-15 dark:opacity-20 pointer-events-none" />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center max-w-3xl mx-auto">
            {/* Pill badge */}
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold bg-brand-50 dark:bg-brand-950/60 border border-brand-200 dark:border-brand-800 text-brand-700 dark:text-brand-300 mb-6 shadow-sm">
              <span className="flex h-2 w-2 rounded-full bg-brand-500 animate-pulse" />
              <span>Production-Grade Queue Orchestration Engine</span>
              <ChevronRight className="w-3.5 h-3.5 text-brand-500" />
            </div>

            <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight leading-[1.1] text-slate-950 dark:text-white">
              Zero Queues.{' '}
              <span className="bg-gradient-to-r from-brand-600 via-sky-500 to-indigo-600 bg-clip-text text-transparent">
                Real-Time Flow.
              </span>
            </h1>

            <p className="mt-6 text-lg sm:text-xl text-slate-600 dark:text-slate-300 leading-relaxed font-normal">
              High-throughput queue management platform built for modern hospitals, universities, banks and government bureaus. Powered by Spring WebSockets, priority scheduling, and concurrency-safe transactional counters.
            </p>

            {/* Quick Demo Access Bar */}
            <div className="mt-10 p-5 rounded-2xl bg-white/90 dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 shadow-xl backdrop-blur-md">
              <div className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-3 flex items-center justify-center gap-2">
                <Sparkles className="w-4 h-4 text-amber-500" />
                <span>Instant Interactive Demo Personas</span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <button
                  disabled={demoLoading}
                  onClick={() => handleQuickDemoLogin('customer@queueless.com', 'customer123', '/dashboard')}
                  className="flex items-center justify-between p-3.5 rounded-xl border border-emerald-200 dark:border-emerald-900/50 bg-emerald-50/50 dark:bg-emerald-950/30 hover:bg-emerald-100/60 dark:hover:bg-emerald-900/40 text-left transition group"
                >
                  <div>
                    <div className="text-xs font-bold text-emerald-900 dark:text-emerald-300">Customer / Student</div>
                    <div className="text-[11px] text-slate-500 dark:text-slate-400">Book token & track live</div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-emerald-600 dark:text-emerald-400 group-hover:translate-x-1 transition-transform" />
                </button>

                <button
                  disabled={demoLoading}
                  onClick={() => handleQuickDemoLogin('staff.sarah@hospital.com', 'staff123', '/staff')}
                  className="flex items-center justify-between p-3.5 rounded-xl border border-blue-200 dark:border-blue-900/50 bg-blue-50/50 dark:bg-blue-950/30 hover:bg-blue-100/60 dark:hover:bg-blue-900/40 text-left transition group"
                >
                  <div>
                    <div className="text-xs font-bold text-blue-900 dark:text-blue-300">Staff Counter Terminal</div>
                    <div className="text-[11px] text-slate-500 dark:text-slate-400">Call, recall & serve</div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-blue-600 dark:text-blue-400 group-hover:translate-x-1 transition-transform" />
                </button>

                <button
                  disabled={demoLoading}
                  onClick={() => handleQuickDemoLogin('admin@queueless.com', 'admin123', '/admin')}
                  className="flex items-center justify-between p-3.5 rounded-xl border border-purple-200 dark:border-purple-900/50 bg-purple-50/50 dark:bg-purple-950/30 hover:bg-purple-100/60 dark:hover:bg-purple-900/40 text-left transition group"
                >
                  <div>
                    <div className="text-xs font-bold text-purple-900 dark:text-purple-300">Executive Administrator</div>
                    <div className="text-[11px] text-slate-500 dark:text-slate-400">Analytics, orgs & SLAs</div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-purple-600 dark:text-purple-400 group-hover:translate-x-1 transition-transform" />
                </button>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-center gap-4 text-xs">
                <Link to="/board" className="font-semibold text-brand-600 dark:text-brand-400 hover:underline flex items-center gap-1.5">
                  <Monitor className="w-3.5 h-3.5" />
                  <span>Launch Live Public Waiting Display</span>
                </Link>
                <span className="text-slate-300 dark:text-slate-700">•</span>
                <Link to="/register" className="font-semibold text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white">
                  Create New Account
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Available Organizations Section */}
      <section className="py-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-slate-950 dark:text-white">
              Connected Service Centers
            </h2>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              Select any institution to view current departments, active counters, and real-time wait times.
            </p>
          </div>

          <Link
            to="/board"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-slate-900 text-white dark:bg-slate-100 dark:text-slate-900 hover:opacity-90 transition shadow-sm"
          >
            <Activity className="w-4 h-4 text-emerald-400 dark:text-emerald-600" />
            <span>Open Public Display</span>
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-pulse">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-48 rounded-2xl bg-slate-200 dark:bg-slate-800" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {organizations.map(org => {
              const Icon = getOrgIcon(org.type);
              return (
                <div
                  key={org.id}
                  className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl p-6 shadow-sm hover:shadow-md hover:border-brand-500/40 dark:hover:border-brand-500/40 transition group flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between mb-4">
                      <div className="w-10 h-10 rounded-xl bg-brand-500/10 text-brand-600 dark:text-brand-400 flex items-center justify-center font-bold">
                        <Icon className="w-5 h-5" />
                      </div>
                      <span className="text-[11px] font-mono font-semibold px-2.5 py-1 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                        {org.code}
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-slate-900 dark:text-white group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
                      {org.name}
                    </h3>
                    <p className="mt-2 text-xs text-slate-500 dark:text-slate-400 line-clamp-2">
                      {org.description}
                    </p>
                  </div>

                  <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between">
                    <span className="text-[11px] font-medium text-slate-400">
                      {org.address?.split(',')[0]}
                    </span>
                    <Link
                      to={`/board?org=${org.id}`}
                      className="inline-flex items-center gap-1 text-xs font-bold text-brand-600 dark:text-brand-400 hover:underline"
                    >
                      <span>Live Board</span>
                      <ArrowUpRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {/* Features Grid */}
      <section className="py-16 bg-slate-100/60 dark:bg-slate-900/40 border-t border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600 dark:text-brand-400">
              Engineered for Mission-Critical Reliability
            </h2>
            <p className="mt-2 text-2xl font-extrabold text-slate-950 dark:text-white">
              Built to withstand real-world operational rushes.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800">
              <div className="w-10 h-10 rounded-xl bg-sky-500/10 text-sky-600 dark:text-sky-400 flex items-center justify-center mb-4">
                <Zap className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">Atomic Concurrency</h3>
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                Pessimistic locking and atomic query transitions prevent race conditions when multiple staff counters call customers at the exact same instant.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mb-4">
                <Clock className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">Adaptive Wait Estimator</h3>
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                Calculates live ETA for customers based on actual historical service durations, active counter capacity, and priority tier weights.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800">
              <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 dark:text-purple-400 flex items-center justify-center mb-4">
                <Activity className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">Live STOMP WebSockets</h3>
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                Zero polling or page refreshes. Token status changes, counter calls, and queue movements broadcast instantly to waiting screens and mobile phones.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
