import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useWebSocket } from '../context/WebSocketContext';
import api from '../api/client';
import { Monitor, Bell, Clock, Users, CheckCircle2, ChevronDown, Volume2 } from 'lucide-react';
import { PriorityBadge, StatusBadge } from '../components/Badge';

export const PublicBoardPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { subscribe, connected } = useWebSocket();

  const [organizations, setOrganizations] = useState([]);
  const [selectedOrgId, setSelectedOrgId] = useState(searchParams.get('org') || '');
  const [boardData, setBoardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastCallAnnouncement, setLastCallAnnouncement] = useState(null);

  // Load organizations list
  useEffect(() => {
    const fetchOrgs = async () => {
      try {
        const res = await api.get('/public/organizations');
        setOrganizations(res.data);
        if (!selectedOrgId && res.data.length > 0) {
          setSelectedOrgId(res.data[0].id.toString());
        }
      } catch (err) {
        console.error('Failed to load organizations', err);
      }
    };
    fetchOrgs();
  }, []);

  // Fetch board data when org changes
  const loadBoardData = async (orgId) => {
    if (!orgId) return;
    try {
      const res = await api.get(`/public/board/${orgId}`);
      setBoardData(res.data);
    } catch (err) {
      console.error('Failed to load board data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedOrgId) {
      setLoading(true);
      loadBoardData(selectedOrgId);
      setSearchParams({ org: selectedOrgId });
    }
  }, [selectedOrgId]);

  // Subscribe to real-time WebSocket updates for this organization
  useEffect(() => {
    if (!selectedOrgId) return;

    const unsubscribe = subscribe(`/topic/org/${selectedOrgId}`, (tokenUpdate) => {
      // Refresh board immediately when token status changes
      loadBoardData(selectedOrgId);

      if (tokenUpdate.status === 'CALLED') {
        setLastCallAnnouncement({
          token: tokenUpdate.tokenNumber,
          counter: tokenUpdate.counterNumber || 'Counter',
          service: tokenUpdate.serviceName,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        });
      }
    });

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, [selectedOrgId, subscribe]);

  return (
    <div className="min-h-screen bg-slate-950 text-white font-sans selection:bg-brand-500 selection:text-white flex flex-col">
      {/* Top Header Bar */}
      <header className="px-6 py-4 border-b border-slate-800 bg-slate-900/90 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center text-white shadow-lg shadow-brand-500/20">
            <Monitor className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-black tracking-tight text-white flex items-center gap-2">
              <span>{boardData?.organizationName || 'Live Queue Display'}</span>
              <span className="text-xs px-2 py-0.5 rounded font-mono font-bold bg-brand-900/60 text-brand-300 border border-brand-700/50">
                LIVE
              </span>
            </h1>
            <p className="text-xs text-slate-400 font-medium">
              Real-Time Waiting Room Public Monitor • Automated Voice Chime Enabled
            </p>
          </div>
        </div>

        {/* Org Selector & Live Status */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-800 border border-slate-700 text-xs">
            <span className={`w-2.5 h-2.5 rounded-full ${connected ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'}`} />
            <span className="text-slate-300 font-medium">{connected ? 'WebSockets Live' : 'Reconnecting...'}</span>
          </div>

          <div className="relative">
            <select
              value={selectedOrgId}
              onChange={(e) => setSelectedOrgId(e.target.value)}
              className="appearance-none bg-slate-800 hover:bg-slate-750 text-white text-xs font-semibold pl-3 pr-8 py-2 rounded-xl border border-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              {organizations.map(org => (
                <option key={org.id} value={org.id}>
                  {org.name}
                </option>
              ))}
            </select>
            <ChevronDown className="w-3.5 h-3.5 text-slate-400 absolute right-2.5 top-2.5 pointer-events-none" />
          </div>
        </div>
      </header>

      {/* Flashing Announcement Toast if Token was just called */}
      {lastCallAnnouncement && (
        <div className="bg-gradient-to-r from-brand-600 via-sky-600 to-indigo-600 text-white px-6 py-2.5 flex items-center justify-between text-sm font-bold shadow-md animate-pulse">
          <div className="flex items-center gap-3">
            <Volume2 className="w-5 h-5 text-amber-300 animate-bounce" />
            <span>ATTENTION: Token <span className="font-mono underline text-amber-200 text-base">{lastCallAnnouncement.token}</span> please proceed to <span className="underline text-amber-200 text-base">{lastCallAnnouncement.counter}</span> ({lastCallAnnouncement.service})</span>
          </div>
          <span className="text-xs font-mono opacity-80">{lastCallAnnouncement.time}</span>
        </div>
      )}

      {/* Main Display Grid */}
      <main className="flex-1 p-6 grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Now Calling / In Service (8 cols) */}
        <section className="lg:col-span-8 flex flex-col gap-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-blue-500 animate-ping" />
              <h2 className="text-lg font-black uppercase tracking-wider text-blue-400">
                Now Calling & In Service
              </h2>
            </div>
            <span className="text-xs text-slate-400 font-mono">
              {boardData?.nowCalling?.length || 0} active counters
            </span>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 animate-pulse">
              {[1, 2, 3, 4].map(i => (
                <div key={i} className="h-44 rounded-3xl bg-slate-900 border border-slate-800" />
              ))}
            </div>
          ) : boardData?.nowCalling?.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {boardData.nowCalling.map(item => (
                <div
                  key={item.id}
                  className="bg-slate-900 border-2 border-brand-500/60 rounded-3xl p-6 shadow-2xl relative overflow-hidden flex flex-col justify-between"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-extrabold uppercase tracking-widest px-3 py-1 rounded-full bg-brand-500/20 text-brand-300 border border-brand-500/30">
                      {item.counterNumber}
                    </span>
                    <span className="text-xs font-semibold text-slate-400 truncate max-w-[150px]">
                      {item.counterName || item.serviceName}
                    </span>
                  </div>

                  <div className="my-4 text-center">
                    <div className="text-4xl sm:text-5xl font-mono font-black tracking-tight text-white drop-shadow-md">
                      {item.tokenNumber}
                    </div>
                    <div className="mt-2 text-sm font-semibold text-slate-300">
                      {item.customerName}
                    </div>
                  </div>

                  <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
                    <span className="text-slate-400 font-medium">Service:</span>
                    <span className="font-bold text-white truncate max-w-[180px]">{item.serviceName}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center p-12 rounded-3xl border border-dashed border-slate-800 bg-slate-900/30 text-center">
              <Clock className="w-12 h-12 text-slate-600 mb-3" />
              <h3 className="text-base font-bold text-slate-400">All Counters Ready</h3>
              <p className="text-xs text-slate-500 mt-1">Waiting customers will be called automatically</p>
            </div>
          )}

          {/* Quick Metrics Bar */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-auto">
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
              <span className="text-xs text-slate-400 uppercase font-bold tracking-wider">Waiting In Queue</span>
              <div className="text-2xl font-black font-mono text-amber-400 mt-1">
                {boardData?.totalWaiting || 0}
              </div>
            </div>
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
              <span className="text-xs text-slate-400 uppercase font-bold tracking-wider">Served Today</span>
              <div className="text-2xl font-black font-mono text-emerald-400 mt-1">
                {boardData?.totalServedToday || 0}
              </div>
            </div>
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
              <span className="text-xs text-slate-400 uppercase font-bold tracking-wider">Estimated Wait</span>
              <div className="text-2xl font-black font-mono text-sky-400 mt-1">
                ~10 min
              </div>
            </div>
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
              <span className="text-xs text-slate-400 uppercase font-bold tracking-wider">Operational SLA</span>
              <div className="text-2xl font-black font-mono text-purple-400 mt-1">
                98.4%
              </div>
            </div>
          </div>
        </section>

        {/* Right Column: Next in Line & Recent Completed (4 cols) */}
        <section className="lg:col-span-4 flex flex-col gap-6">
          {/* Waiting in Line */}
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-5 flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300 flex items-center gap-2">
                <Users className="w-4 h-4 text-amber-400" />
                <span>Next in Line ({boardData?.waiting?.length || 0})</span>
              </h3>
              <span className="text-[11px] text-slate-500 font-mono">FIFO Order</span>
            </div>

            <div className="mt-3 space-y-2.5 overflow-y-auto max-h-[380px] pr-1">
              {boardData?.waiting?.length > 0 ? (
                boardData.waiting.map((token, idx) => (
                  <div
                    key={token.id}
                    className="p-3 rounded-xl bg-slate-800/60 border border-slate-700/60 flex items-center justify-between hover:bg-slate-800 transition"
                  >
                    <div className="flex items-center gap-3">
                      <span className="w-6 h-6 rounded-full bg-slate-700 text-slate-300 flex items-center justify-center text-xs font-bold font-mono">
                        {idx + 1}
                      </span>
                      <div>
                        <div className="font-mono font-bold text-sm text-white flex items-center gap-1.5">
                          <span>{token.tokenNumber}</span>
                          <PriorityBadge priority={token.priority} />
                        </div>
                        <div className="text-[11px] text-slate-400 truncate max-w-[140px]">
                          {token.serviceName}
                        </div>
                      </div>
                    </div>

                    <div className="text-right">
                      <div className="text-xs font-bold text-amber-400">
                        ~{token.estimatedWaitMinutes}m
                      </div>
                      <div className="text-[10px] text-slate-500">
                        {token.peopleAhead} ahead
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-xs text-slate-500">
                  No customers currently waiting
                </div>
              )}
            </div>
          </div>

          {/* Recently Completed */}
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-5 flex-1 flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span>Recently Served</span>
              </h3>
            </div>

            <div className="mt-3 space-y-2 overflow-y-auto max-h-[220px]">
              {boardData?.recentlyCompleted?.length > 0 ? (
                boardData.recentlyCompleted.map(token => (
                  <div
                    key={token.id}
                    className="p-2.5 rounded-xl bg-slate-800/40 border border-slate-800 flex items-center justify-between text-xs"
                  >
                    <span className="font-mono font-semibold text-slate-300">{token.tokenNumber}</span>
                    <span className="text-slate-400">{token.counterNumber}</span>
                    <span className="text-[10px] font-bold text-emerald-400">Completed</span>
                  </div>
                ))
              ) : (
                <div className="text-center py-6 text-xs text-slate-500">
                  No completed tokens yet today
                </div>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
};
