import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import api from '../api/client';
import {
  PhoneCall, Play, CheckCircle2, UserX, AlertCircle, RefreshCw,
  Clock, Users, Bell, FileText, ChevronRight, Sparkles, AlertTriangle
} from 'lucide-react';
import { StatusBadge, PriorityBadge } from '../components/Badge';
import { Modal } from '../components/Modal';
import { EmptyState } from '../components/EmptyState';

export const StaffDashboard = () => {
  const { user } = useAuth();
  const { subscribe } = useWebSocket();

  const [counter, setCounter] = useState(null);
  const [activeToken, setActiveToken] = useState(null);
  const [waitingTokens, setWaitingTokens] = useState([]);
  const [completedTokens, setCompletedTokens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Complete Service Modal
  const [isCompleteModalOpen, setIsCompleteModalOpen] = useState(false);
  const [completionNotes, setCompletionNotes] = useState('');

  // No show confirmation modal
  const [isNoShowModalOpen, setIsNoShowModalOpen] = useState(false);

  // Load counter & queues
  const loadStaffTerminal = async () => {
    try {
      setLoading(true);
      setErrorMsg('');

      // 1. Get staff counter
      const counterRes = await api.get('/staff/my-counter');
      const counterData = counterRes.data;
      setCounter(counterData);

      // 2. Fetch active token & waiting queue & completed tokens
      const [activeRes, waitingRes, completedRes] = await Promise.all([
        api.get(`/staff/active-token?counterId=${counterData.id}`),
        api.get(`/staff/waiting-tokens?departmentId=${counterData.departmentId}`),
        api.get(`/staff/completed-tokens?counterId=${counterData.id}`)
      ]);

      setActiveToken(activeRes.data || null);
      setWaitingTokens(waitingRes.data || []);
      setCompletedTokens(completedRes.data || []);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to load staff terminal data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStaffTerminal();
  }, []);

  // Subscribe to WebSocket updates for this counter & department
  useEffect(() => {
    if (!counter) return;

    // Listen to counter topic
    const unsubCounter = subscribe(`/topic/counter/${counter.id}`, () => {
      loadStaffTerminal();
    });

    // Listen to org topic to refresh waiting list when new tokens arrive
    const unsubOrg = subscribe(`/topic/org/${counter.organizationId}`, () => {
      api.get(`/staff/waiting-tokens?departmentId=${counter.departmentId}`)
        .then(res => setWaitingTokens(res.data || []));
    });

    return () => {
      if (unsubCounter) unsubCounter();
      if (unsubOrg) unsubOrg();
    };
  }, [counter?.id, subscribe]);

  // Actions
  const handleCallNext = async () => {
    if (!counter) return;
    setErrorMsg('');
    setActionLoading(true);
    try {
      const res = await api.post(`/staff/call-next?counterId=${counter.id}`);
      setActiveToken(res.data);
      // Refresh waiting queue
      const waitingRes = await api.get(`/staff/waiting-tokens?departmentId=${counter.departmentId}`);
      setWaitingTokens(waitingRes.data || []);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error calling next customer');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRecall = async () => {
    if (!activeToken || !counter) return;
    setErrorMsg('');
    setActionLoading(true);
    try {
      const res = await api.post(`/staff/tokens/${activeToken.id}/recall?counterId=${counter.id}`);
      setActiveToken(res.data);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error recalling token');
    } finally {
      setActionLoading(false);
    }
  };

  const handleStartServing = async () => {
    if (!activeToken || !counter) return;
    setErrorMsg('');
    setActionLoading(true);
    try {
      const res = await api.post(`/staff/tokens/${activeToken.id}/start-serving?counterId=${counter.id}`);
      setActiveToken(res.data);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error starting service');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCompleteService = async () => {
    if (!activeToken || !counter) return;
    setErrorMsg('');
    setActionLoading(true);
    try {
      await api.post(`/staff/tokens/${activeToken.id}/complete?counterId=${counter.id}`, {
        notes: completionNotes
      });
      setIsCompleteModalOpen(false);
      setCompletionNotes('');
      loadStaffTerminal();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error completing token');
    } finally {
      setActionLoading(false);
    }
  };

  const handleMarkNoShow = async () => {
    if (!activeToken || !counter) return;
    setErrorMsg('');
    setActionLoading(true);
    try {
      await api.post(`/staff/tokens/${activeToken.id}/no-show?counterId=${counter.id}`, {
        notes: 'Customer absent after repeated counter announcements'
      });
      setIsNoShowModalOpen(false);
      loadStaffTerminal();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error marking no-show');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <div className="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-xs text-slate-500 dark:text-slate-400">Loading your service counter terminal...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner: Counter Identity & Quick Actions */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Active Counter Station
            </span>
          </div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-white flex items-center gap-3">
            <span>{counter?.counterNumber}: {counter?.name}</span>
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Operator: <strong>{user?.fullName}</strong> • Department: <strong>{counter?.departmentName}</strong>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={loadStaffTerminal}
            className="p-2.5 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            title="Refresh Terminal"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {errorMsg && (
        <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Primary Work Area Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Current Active Customer Terminal (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 shadow-sm">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
              <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <Users className="w-4 h-4 text-brand-600" />
                <span>Current Service Desk</span>
              </h2>

              {activeToken && (
                <div className="flex items-center gap-2">
                  <PriorityBadge priority={activeToken.priority} />
                  <StatusBadge status={activeToken.status} />
                </div>
              )}
            </div>

            {activeToken ? (
              <div className="py-6 space-y-6">
                <div className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800 text-center">
                  <span className="text-xs uppercase font-bold tracking-widest text-slate-400">Serving Token</span>
                  <div className="text-5xl sm:text-6xl font-mono font-black tracking-tight text-slate-900 dark:text-white mt-1">
                    {activeToken.tokenNumber}
                  </div>
                  <div className="mt-3 text-sm font-bold text-slate-800 dark:text-slate-200">
                    {activeToken.customerName}
                  </div>
                  <div className="text-xs text-slate-500 font-mono mt-0.5">
                    {activeToken.customerPhone || 'Walk-in'} • {activeToken.serviceName}
                  </div>
                </div>

                {/* Status-specific action controls */}
                <div className="flex flex-wrap items-center justify-center gap-3">
                  {activeToken.status === 'CALLED' && (
                    <>
                      <button
                        onClick={handleRecall}
                        disabled={actionLoading}
                        className="flex-1 py-3 px-4 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 transition flex items-center justify-center gap-2"
                      >
                        <Bell className="w-4 h-4 text-amber-500" />
                        <span>Recall Customer ({activeToken.recallCount || 1})</span>
                      </button>

                      <button
                        onClick={handleStartServing}
                        disabled={actionLoading}
                        className="flex-1 py-3 px-4 rounded-xl text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-md transition flex items-center justify-center gap-2"
                      >
                        <Play className="w-4 h-4" />
                        <span>Start Serving</span>
                      </button>

                      <button
                        onClick={() => setIsNoShowModalOpen(true)}
                        disabled={actionLoading}
                        className="py-3 px-4 rounded-xl text-xs font-bold border border-rose-300 dark:border-rose-900/50 text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition flex items-center justify-center gap-2"
                      >
                        <UserX className="w-4 h-4" />
                        <span>No Show</span>
                      </button>
                    </>
                  )}

                  {activeToken.status === 'SERVING' && (
                    <button
                      onClick={() => setIsCompleteModalOpen(true)}
                      disabled={actionLoading}
                      className="w-full py-3.5 px-6 rounded-xl text-sm font-bold bg-brand-600 hover:bg-brand-700 text-white shadow-lg shadow-brand-500/25 transition flex items-center justify-center gap-2"
                    >
                      <CheckCircle2 className="w-5 h-5" />
                      <span>Complete Service & Clear Desk</span>
                    </button>
                  )}
                </div>
              </div>
            ) : (
              <div className="py-12 text-center space-y-4">
                <div className="w-16 h-16 rounded-3xl bg-brand-50 dark:bg-brand-950/40 border border-brand-200 dark:border-brand-900 text-brand-600 dark:text-brand-400 flex items-center justify-center mx-auto">
                  <PhoneCall className="w-8 h-8" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                    Counter Ready for Next Customer
                  </h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 max-w-sm mx-auto">
                    {waitingTokens.length > 0
                      ? `${waitingTokens.length} customer(s) waiting in your department queue.`
                      : 'The waiting queue is currently clear.'}
                  </p>
                </div>

                <button
                  onClick={handleCallNext}
                  disabled={actionLoading || waitingTokens.length === 0}
                  className="py-3 px-8 rounded-xl text-sm font-bold bg-brand-600 hover:bg-brand-700 text-white shadow-lg shadow-brand-500/25 transition inline-flex items-center gap-2 disabled:opacity-40"
                >
                  <PhoneCall className="w-4 h-4" />
                  <span>Call Next Customer</span>
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Waiting Queue & Today Completed (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Waiting Queue List */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-5 shadow-sm flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <Users className="w-4 h-4 text-amber-500" />
                <span>Department Waiting Queue ({waitingTokens.length})</span>
              </h3>
              <span className="text-[11px] font-mono text-slate-400">Priority FIFO</span>
            </div>

            <div className="mt-3 space-y-2 overflow-y-auto max-h-[300px]">
              {waitingTokens.length > 0 ? (
                waitingTokens.map((t, idx) => (
                  <div
                    key={t.id}
                    className="p-3 rounded-xl bg-slate-50 dark:bg-slate-950/50 border border-slate-200/80 dark:border-slate-800 flex items-center justify-between"
                  >
                    <div className="flex items-center gap-2.5">
                      <span className="w-5 h-5 rounded-full bg-slate-200 dark:bg-slate-800 text-[11px] font-bold font-mono flex items-center justify-center text-slate-600 dark:text-slate-400">
                        {idx + 1}
                      </span>
                      <div>
                        <div className="font-mono font-bold text-xs text-slate-900 dark:text-white flex items-center gap-1.5">
                          <span>{t.tokenNumber}</span>
                          <PriorityBadge priority={t.priority} />
                        </div>
                        <div className="text-[11px] text-slate-500 truncate max-w-[150px]">
                          {t.customerName}
                        </div>
                      </div>
                    </div>

                    <div className="text-right text-[11px]">
                      <span className="font-bold text-amber-600 dark:text-amber-400">
                        ~{t.estimatedWaitMinutes}m
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-xs text-slate-400">
                  No customers currently waiting
                </div>
              )}
            </div>
          </div>

          {/* Today Completed at this counter */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-5 shadow-sm">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                <span>Completed by You Today ({completedTokens.length})</span>
              </h3>
            </div>

            <div className="mt-3 space-y-2 overflow-y-auto max-h-[220px]">
              {completedTokens.length > 0 ? (
                completedTokens.map(t => (
                  <div
                    key={t.id}
                    className="p-2.5 rounded-xl border border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs"
                  >
                    <span className="font-mono font-semibold text-slate-800 dark:text-slate-200">{t.tokenNumber}</span>
                    <span className="text-slate-500 truncate max-w-[120px]">{t.customerName}</span>
                    <StatusBadge status={t.status} />
                  </div>
                ))
              ) : (
                <div className="text-center py-6 text-xs text-slate-400">
                  No completed tokens yet today
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Complete Service Modal */}
      <Modal
        isOpen={isCompleteModalOpen}
        onClose={() => setIsCompleteModalOpen(false)}
        title="Complete Service Session"
      >
        <div className="space-y-4">
          <p className="text-xs text-slate-600 dark:text-slate-400">
            Confirming completion for token <strong className="font-mono">{activeToken?.tokenNumber}</strong> ({activeToken?.customerName}).
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Service Notes / Resolution (Optional)
            </label>
            <textarea
              rows={3}
              value={completionNotes}
              onChange={(e) => setCompletionNotes(e.target.value)}
              placeholder="e.g. Prescribed medication, documents stamped, transaction cleared..."
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              onClick={() => setIsCompleteModalOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleCompleteService}
              disabled={actionLoading}
              className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md transition"
            >
              {actionLoading ? 'Saving...' : 'Mark Completed'}
            </button>
          </div>
        </div>
      </Modal>

      {/* No Show Modal */}
      <Modal
        isOpen={isNoShowModalOpen}
        onClose={() => setIsNoShowModalOpen(false)}
        title="Mark Customer as No-Show"
        maxWidth="max-w-sm"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 text-amber-600 dark:text-amber-400">
            <AlertTriangle className="w-8 h-8 shrink-0" />
            <p className="text-xs text-slate-600 dark:text-slate-300">
              Mark token <strong className="font-mono">{activeToken?.tokenNumber}</strong> as Absent / No-Show? The customer will forfeit their place in queue.
            </p>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              onClick={() => setIsNoShowModalOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleMarkNoShow}
              className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 shadow-sm"
            >
              Confirm Absent
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
