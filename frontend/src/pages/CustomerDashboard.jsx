import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import api from '../api/client';
import {
  Ticket, Clock, Users, ArrowRight, XCircle, CheckCircle2,
  AlertCircle, Building2, Layers, RefreshCw, Calendar, Sparkles, AlertTriangle
} from 'lucide-react';
import { StatusBadge, PriorityBadge } from '../components/Badge';
import { Modal } from '../components/Modal';
import { EmptyState } from '../components/EmptyState';

export const CustomerDashboard = () => {
  const { user } = useAuth();
  const { subscribe } = useWebSocket();

  // State
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrg, setSelectedOrg] = useState(null);
  const [services, setServices] = useState([]);
  const [selectedService, setSelectedService] = useState(null);

  // My Tokens & Active Token
  const [myTokens, setMyTokens] = useState([]);
  const [activeToken, setActiveToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Generate Token Modal
  const [isBookModalOpen, setIsBookModalOpen] = useState(false);
  const [customerName, setCustomerName] = useState(user?.fullName || '');
  const [customerPhone, setCustomerPhone] = useState(user?.phoneNumber || '');
  const [priority, setPriority] = useState('REGULAR');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Cancel confirmation
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [tokenToCancel, setTokenToCancel] = useState(null);

  // Fetch initial data
  const loadCustomerData = async () => {
    try {
      setLoading(true);
      const [orgsRes, tokensRes] = await Promise.all([
        api.get('/public/organizations'),
        api.get('/customer/tokens')
      ]);

      setOrganizations(orgsRes.data);
      if (orgsRes.data.length > 0 && !selectedOrg) {
        setSelectedOrg(orgsRes.data[0]);
      }

      setMyTokens(tokensRes.data);
      const active = tokensRes.data.find(t =>
        t.status === 'WAITING' || t.status === 'CALLED' || t.status === 'SERVING'
      );
      setActiveToken(active || null);
    } catch (err) {
      console.error('Failed to load customer data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomerData();
  }, []);

  // Fetch services when org changes
  useEffect(() => {
    if (selectedOrg) {
      api.get(`/public/organizations/${selectedOrg.id}/services`)
        .then(res => setServices(res.data))
        .catch(err => console.error(err));
    }
  }, [selectedOrg]);

  // Subscribe to updates for active token
  useEffect(() => {
    if (!activeToken) return;

    const unsub = subscribe(`/topic/token/${activeToken.id}`, (updatedToken) => {
      setActiveToken(updatedToken);
      // Refresh token list
      api.get('/customer/tokens').then(res => setMyTokens(res.data));
    });

    return () => {
      if (unsub) unsub();
    };
  }, [activeToken?.id, subscribe]);

  const handleOpenBookModal = (service) => {
    setSelectedService(service);
    setErrorMsg('');
    setCustomerName(user?.fullName || '');
    setCustomerPhone(user?.phoneNumber || '');
    setIsBookModalOpen(true);
  };

  const handleBookToken = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSubmitting(true);

    try {
      const payload = {
        serviceId: selectedService.id,
        customerName,
        customerPhone,
        priority
      };

      const res = await api.post('/customer/tokens', payload);
      setActiveToken(res.data);
      setMyTokens(prev => [res.data, ...prev]);
      setIsBookModalOpen(false);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Could not generate token. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelToken = async () => {
    if (!tokenToCancel) return;
    try {
      await api.post(`/customer/tokens/${tokenToCancel.id}/cancel`, { notes: 'Cancelled by customer' });
      setIsCancelModalOpen(false);
      setTokenToCancel(null);
      loadCustomerData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel token');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Welcome Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Welcome, {user?.fullName || 'Customer'}
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Select a service to generate your real-time token or track active bookings.
          </p>
        </div>

        <button
          onClick={loadCustomerData}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-850 shadow-sm transition"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Refresh Queue</span>
        </button>
      </div>

      {/* Live Active Token Tracker Banner */}
      {activeToken && (
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-brand-900 via-slate-900 to-indigo-950 border-2 border-brand-500/50 p-6 sm:p-8 text-white shadow-2xl">
          <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 rounded-full bg-brand-500/20 blur-3xl pointer-events-none" />

          <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
                <span className="text-xs font-bold uppercase tracking-widest text-brand-300">
                  Live Queue Tracker
                </span>
                <StatusBadge status={activeToken.status} />
                <PriorityBadge priority={activeToken.priority} />
              </div>

              <div>
                <span className="text-xs text-slate-400 font-medium">Your Token Number</span>
                <div className="text-4xl sm:text-6xl font-mono font-black tracking-tight text-white mt-1">
                  {activeToken.tokenNumber}
                </div>
              </div>

              <div className="text-xs text-slate-300 flex flex-wrap items-center gap-4">
                <span><strong>Service:</strong> {activeToken.serviceName}</span>
                <span>•</span>
                <span><strong>Organization:</strong> {activeToken.organizationName}</span>
                {activeToken.counterNumber && (
                  <>
                    <span>•</span>
                    <span className="text-amber-300 font-bold underline">
                      Proceed to {activeToken.counterNumber} ({activeToken.counterName})
                    </span>
                  </>
                )}
              </div>
            </div>

            {/* Waiting metrics */}
            <div className="w-full md:w-auto flex flex-wrap items-center gap-4 bg-white/5 border border-white/10 p-4 rounded-2xl backdrop-blur-md">
              <div className="text-center px-4">
                <div className="text-xs text-slate-400 uppercase font-semibold">People Ahead</div>
                <div className="text-3xl font-extrabold font-mono text-amber-400 mt-1">
                  {activeToken.peopleAhead ?? 0}
                </div>
              </div>

              <div className="h-10 w-[1px] bg-white/10 hidden sm:block" />

              <div className="text-center px-4">
                <div className="text-xs text-slate-400 uppercase font-semibold">Estimated Wait</div>
                <div className="text-3xl font-extrabold font-mono text-sky-400 mt-1">
                  ~{activeToken.estimatedWaitMinutes} <span className="text-sm font-sans">min</span>
                </div>
              </div>

              {activeToken.status === 'WAITING' && (
                <button
                  onClick={() => {
                    setTokenToCancel(activeToken);
                    setIsCancelModalOpen(true);
                  }}
                  className="w-full sm:w-auto mt-2 sm:mt-0 px-4 py-2.5 rounded-xl text-xs font-bold bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border border-rose-500/40 transition flex items-center justify-center gap-1.5"
                >
                  <XCircle className="w-4 h-4" />
                  <span>Cancel Token</span>
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Service Catalog Section */}
      <section className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">
              Browse Available Services
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Select an organization below to view and book tokens for their service departments.
            </p>
          </div>

          {/* Org Selector Tabs */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {organizations.map(org => (
              <button
                key={org.id}
                onClick={() => setSelectedOrg(org)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition ${
                  selectedOrg?.id === org.id
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
              >
                {org.name}
              </button>
            ))}
          </div>
        </div>

        {/* Services Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {services.map(svc => (
            <div
              key={svc.id}
              className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl p-5 shadow-sm hover:shadow-md hover:border-brand-500/40 transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-[11px] font-mono font-semibold px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                    {svc.code}
                  </span>
                  <div className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                    <Clock className="w-3.5 h-3.5" />
                    <span>~{svc.estimatedDurationMinutes} mins</span>
                  </div>
                </div>

                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  {svc.name}
                </h3>
                <p className="mt-1 text-xs text-slate-500 dark:text-slate-400 line-clamp-2">
                  {svc.description}
                </p>
              </div>

              <div className="mt-5 pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <div className="text-xs">
                  <span className="text-slate-400">Waiting: </span>
                  <span className="font-bold font-mono text-slate-800 dark:text-slate-200">
                    {svc.currentWaitingCount || 0} in queue
                  </span>
                </div>

                <button
                  onClick={() => handleOpenBookModal(svc)}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-sm transition"
                >
                  <Ticket className="w-3.5 h-3.5" />
                  <span>Get Token</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Booking History Section */}
      <section className="space-y-4">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">
          My Token History
        </h2>

        {myTokens.length > 0 ? (
          <div className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
                  <tr>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Token</th>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Service</th>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Counter</th>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Priority</th>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Status</th>
                    <th className="px-5 py-3.5 font-bold uppercase tracking-wider">Booked At</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {myTokens.map(token => (
                    <tr key={token.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-850/50 transition">
                      <td className="px-5 py-3.5 font-mono font-bold text-slate-900 dark:text-white">
                        {token.tokenNumber}
                      </td>
                      <td className="px-5 py-3.5 text-slate-700 dark:text-slate-300 font-medium">
                        {token.serviceName}
                      </td>
                      <td className="px-5 py-3.5 text-slate-600 dark:text-slate-400">
                        {token.counterNumber || '-'}
                      </td>
                      <td className="px-5 py-3.5">
                        <PriorityBadge priority={token.priority} />
                      </td>
                      <td className="px-5 py-3.5">
                        <StatusBadge status={token.status} />
                      </td>
                      <td className="px-5 py-3.5 text-slate-500 font-mono text-[11px]">
                        {token.createdAt ? new Date(token.createdAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <EmptyState
            icon={Ticket}
            title="No tokens booked yet"
            description="Select any service above to generate your first queue token."
          />
        )}
      </section>

      {/* Book Token Modal */}
      <Modal
        isOpen={isBookModalOpen}
        onClose={() => setIsBookModalOpen(false)}
        title={`Generate Token for ${selectedService?.name}`}
      >
        <form onSubmit={handleBookToken} className="space-y-4">
          {errorMsg && (
            <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Customer / Student Name
            </label>
            <input
              type="text"
              required
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Phone Number (Optional for SMS notifications)
            </label>
            <input
              type="tel"
              value={customerPhone}
              onChange={(e) => setCustomerPhone(e.target.value)}
              placeholder="+1-555-0199"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Queue Priority Level
            </label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="REGULAR">Regular Queue</option>
              <option value="PRIORITY">Priority (Elderly / Fast-Track)</option>
              <option value="VIP">VIP / Emergency Expedited</option>
            </select>
          </div>

          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setIsBookModalOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md transition disabled:opacity-50"
            >
              {submitting ? 'Generating...' : 'Confirm & Generate Token'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Cancel Confirmation Modal */}
      <Modal
        isOpen={isCancelModalOpen}
        onClose={() => setIsCancelModalOpen(false)}
        title="Confirm Token Cancellation"
        maxWidth="max-w-sm"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 text-amber-600 dark:text-amber-400">
            <AlertTriangle className="w-8 h-8 shrink-0" />
            <p className="text-xs text-slate-600 dark:text-slate-300">
              Are you sure you want to cancel token <strong className="font-mono">{tokenToCancel?.tokenNumber}</strong>? Your queue spot will be relinquished.
            </p>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              onClick={() => setIsCancelModalOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Keep My Place
            </button>
            <button
              onClick={handleCancelToken}
              className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 shadow-sm"
            >
              Yes, Cancel Token
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
