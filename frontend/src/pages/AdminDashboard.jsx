import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, AreaChart, Area
} from 'recharts';
import {
  Building2, Users, Clock, CheckCircle2, XCircle, AlertCircle,
  Plus, Edit2, ShieldCheck, UserCheck, Layers, Calendar, Filter,
  Activity, ArrowUpRight, ChevronRight, RefreshCw, Sparkles
} from 'lucide-react';
import { StatCard } from '../components/StatCard';
import { Modal } from '../components/Modal';
import { EmptyState } from '../components/EmptyState';
import { StatusBadge } from '../components/Badge';

export const AdminDashboard = () => {
  const { user } = useAuth();

  // Selected Org & Day Filter
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrgId, setSelectedOrgId] = useState('');
  const [daysFilter, setDaysFilter] = useState(7);
  const [activeTab, setActiveTab] = useState('analytics'); // analytics, departments, counters, staff

  // Analytics data
  const [analytics, setAnalytics] = useState(null);
  const [loadingAnalytics, setLoadingAnalytics] = useState(true);

  // Entities state
  const [departments, setDepartments] = useState([]);
  const [services, setServices] = useState([]);
  const [counters, setCounters] = useState([]);
  const [staffList, setStaffList] = useState([]);

  // Modals state
  const [isDeptModalOpen, setIsDeptModalOpen] = useState(false);
  const [deptName, setDeptName] = useState('');
  const [deptCode, setDeptCode] = useState('');
  const [deptDesc, setDeptDesc] = useState('');

  const [isServiceModalOpen, setIsServiceModalOpen] = useState(false);
  const [targetDeptId, setTargetDeptId] = useState('');
  const [serviceName, setServiceName] = useState('');
  const [serviceCode, setServiceCode] = useState('');
  const [servicePrefix, setServicePrefix] = useState('');
  const [serviceDuration, setServiceDuration] = useState(10);
  const [servicePriority, setServicePriority] = useState(1);
  const [serviceDesc, setServiceDesc] = useState('');

  const [isCounterModalOpen, setIsCounterModalOpen] = useState(false);
  const [counterDeptId, setCounterDeptId] = useState('');
  const [counterNumber, setCounterNumber] = useState('');
  const [counterName, setCounterName] = useState('');

  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [assignCounterId, setAssignCounterId] = useState('');
  const [assignStaffId, setAssignStaffId] = useState('');

  const [isStaffModalOpen, setIsStaffModalOpen] = useState(false);
  const [newStaffName, setNewStaffName] = useState('');
  const [newStaffEmail, setNewStaffEmail] = useState('');
  const [newStaffPass, setNewStaffPass] = useState('');
  const [newStaffPhone, setNewStaffPhone] = useState('');

  // Initial load
  useEffect(() => {
    const fetchOrgs = async () => {
      try {
        const res = await api.get('/admin/organizations');
        setOrganizations(res.data);
        if (res.data.length > 0) {
          const defaultId = user?.organizationId ? user.organizationId.toString() : res.data[0].id.toString();
          setSelectedOrgId(defaultId);
        }
      } catch (err) {
        console.error('Failed to load organizations', err);
      }
    };
    fetchOrgs();
  }, [user]);

  // Load analytics and entities whenever org or days filter changes
  const loadDashboardData = async () => {
    if (!selectedOrgId) return;
    try {
      setLoadingAnalytics(true);
      const [analyticsRes, deptsRes, servicesRes, countersRes, staffRes] = await Promise.all([
        api.get(`/admin/analytics/${selectedOrgId}?days=${daysFilter}`),
        api.get(`/admin/departments/org/${selectedOrgId}`),
        api.get(`/admin/services/org/${selectedOrgId}`),
        api.get(`/admin/counters/org/${selectedOrgId}`),
        api.get(`/admin/staff/${selectedOrgId}`)
      ]);

      setAnalytics(analyticsRes.data);
      setDepartments(deptsRes.data);
      setServices(servicesRes.data);
      setCounters(countersRes.data);
      setStaffList(staffRes.data);

      if (deptsRes.data.length > 0 && !targetDeptId) {
        setTargetDeptId(deptsRes.data[0].id.toString());
        setCounterDeptId(deptsRes.data[0].id.toString());
      }
    } catch (err) {
      console.error('Failed to load admin dashboard data', err);
    } finally {
      setLoadingAnalytics(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [selectedOrgId, daysFilter]);

  // Form Handlers
  const handleCreateDepartment = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/departments', {
        organizationId: Number(selectedOrgId),
        name: deptName,
        code: deptCode,
        description: deptDesc
      });
      setIsDeptModalOpen(false);
      setDeptName('');
      setDeptCode('');
      setDeptDesc('');
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create department');
    }
  };

  const handleCreateService = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/services', {
        departmentId: Number(targetDeptId),
        name: serviceName,
        code: serviceCode,
        prefix: servicePrefix,
        estimatedDurationMinutes: Number(serviceDuration),
        priorityLevel: Number(servicePriority),
        description: serviceDesc
      });
      setIsServiceModalOpen(false);
      setServiceName('');
      setServiceCode('');
      setServicePrefix('');
      setServiceDesc('');
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create service');
    }
  };

  const handleCreateCounter = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/counters', {
        departmentId: Number(counterDeptId),
        counterNumber,
        name: counterName
      });
      setIsCounterModalOpen(false);
      setCounterNumber('');
      setCounterName('');
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create counter');
    }
  };

  const handleAssignStaff = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/counters/assign', {
        counterId: Number(assignCounterId),
        staffId: Number(assignStaffId)
      });
      setIsAssignModalOpen(false);
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to assign staff');
    }
  };

  const handleUnassignStaff = async (counterId) => {
    try {
      await api.post(`/admin/counters/${counterId}/unassign`);
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to unassign staff');
    }
  };

  const handleCreateStaff = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/staff', {
        organizationId: Number(selectedOrgId),
        fullName: newStaffName,
        email: newStaffEmail,
        password: newStaffPass,
        phoneNumber: newStaffPhone,
        role: 'ROLE_STAFF'
      });
      setIsStaffModalOpen(false);
      setNewStaffName('');
      setNewStaffEmail('');
      setNewStaffPass('');
      setNewStaffPhone('');
      loadDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create staff member');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header and Controls */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="w-2 h-2 rounded-full bg-purple-500" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Executive Administration & Control
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white">
            Operations Command Center
          </h1>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Org Selector */}
          <select
            value={selectedOrgId}
            onChange={(e) => setSelectedOrgId(e.target.value)}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 shadow-sm"
          >
            {organizations.map(org => (
              <option key={org.id} value={org.id}>
                {org.name} ({org.code})
              </option>
            ))}
          </select>

          {/* Date range filter */}
          <select
            value={daysFilter}
            onChange={(e) => setDaysFilter(Number(e.target.value))}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 shadow-sm"
          >
            <option value={1}>Last 24 Hours</option>
            <option value={7}>Last 7 Days</option>
            <option value={30}>Last 30 Days</option>
          </select>

          <button
            onClick={loadDashboardData}
            className="p-2 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition"
            title="Refresh Data"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* High-Level Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Tokens"
          value={analytics?.totalTokensGenerated || 0}
          subtitle={`Total generated in last ${daysFilter} days`}
          icon={Layers}
          color="blue"
        />
        <StatCard
          title="Currently Waiting"
          value={analytics?.currentlyWaiting || 0}
          subtitle="Customers currently in queue line"
          icon={Users}
          color="amber"
        />
        <StatCard
          title="Avg Wait Duration"
          value={`${analytics?.avgWaitTimeMinutes || 0}m`}
          subtitle="Average creation-to-call time"
          icon={Clock}
          color="purple"
        />
        <StatCard
          title="Avg Service Duration"
          value={`${analytics?.avgServiceTimeMinutes || 0}m`}
          subtitle="Average counter consultation time"
          icon={Activity}
          color="emerald"
        />
      </div>

      {/* Navigation Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 dark:border-slate-800 pb-3 overflow-x-auto">
        {[
          { id: 'analytics', label: 'Analytics & Performance', icon: Activity },
          { id: 'departments', label: 'Departments & Services', icon: Layers },
          { id: 'counters', label: 'Service Counters', icon: Building2 },
          { id: 'staff', label: 'Staff Management', icon: Users },
        ].map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition ${
                isActive
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab 1: Analytics & Performance */}
      {activeTab === 'analytics' && (
        <div className="space-y-8">
          {/* Charts Row */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            {/* Peak Service Hours Bar Chart (7 cols) */}
            <div className="lg:col-span-7 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h3 className="text-base font-bold text-slate-900 dark:text-white">
                    Peak Hourly Service Demand
                  </h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    Token load distribution across business hours (08:00 - 18:00)
                  </p>
                </div>
              </div>

              <div className="h-64 w-full">
                {analytics?.hourlyDistribution && analytics.hourlyDistribution.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={analytics.hourlyDistribution}>
                      <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                      <XAxis dataKey="hour" fontSize={11} stroke="#94a3b8" />
                      <YAxis fontSize={11} stroke="#94a3b8" />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#0f172a',
                          borderColor: '#334155',
                          borderRadius: '12px',
                          color: '#fff',
                          fontSize: '12px'
                        }}
                      />
                      <Bar dataKey="tokens" fill="#0270c7" radius={[6, 6, 0, 0]} name="Tokens" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="h-full flex items-center justify-center text-xs text-slate-400">
                    No hourly data available
                  </div>
                )}
              </div>
            </div>

            {/* Outcome Metrics Summary (5 cols) */}
            <div className="lg:col-span-5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 shadow-sm flex flex-col justify-between">
              <h3 className="text-base font-bold text-slate-900 dark:text-white mb-4">
                Service Completion & Attrition
              </h3>

              <div className="space-y-4">
                <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <CheckCircle2 className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                    <div>
                      <div className="text-xs font-bold text-emerald-900 dark:text-emerald-300">Completed Sessions</div>
                      <div className="text-[11px] text-slate-500">Successfully handled by staff</div>
                    </div>
                  </div>
                  <span className="text-xl font-extrabold font-mono text-emerald-700 dark:text-emerald-300">
                    {analytics?.completedTokens || 0}
                  </span>
                </div>

                <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <XCircle className="w-5 h-5 text-rose-600 dark:text-rose-400" />
                    <div>
                      <div className="text-xs font-bold text-rose-900 dark:text-rose-300">Cancelled by Customer</div>
                      <div className="text-[11px] text-slate-500">User voluntarily dropped line</div>
                    </div>
                  </div>
                  <span className="text-xl font-extrabold font-mono text-rose-700 dark:text-rose-300">
                    {analytics?.cancelledTokens || 0}
                  </span>
                </div>

                <div className="p-4 rounded-2xl bg-purple-50 dark:bg-purple-950/30 border border-purple-200 dark:border-purple-900 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <Users className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                    <div>
                      <div className="text-xs font-bold text-purple-900 dark:text-purple-300">No-Show / Absent Rate</div>
                      <div className="text-[11px] text-slate-500">{analytics?.noShowTokens || 0} customers absent</div>
                    </div>
                  </div>
                  <span className="text-xl font-extrabold font-mono text-purple-700 dark:text-purple-300">
                    {analytics?.noShowPercentage || 0}%
                  </span>
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-[11px] text-slate-400 text-center">
                Refreshed live via Spring JPA aggregation engine
              </div>
            </div>
          </div>

          {/* Service Performance Table */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 shadow-sm">
            <h3 className="text-base font-bold text-slate-900 dark:text-white mb-4">
              Service Department Performance Breakdown
            </h3>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
                  <tr>
                    <th className="px-4 py-3 font-bold uppercase">Service</th>
                    <th className="px-4 py-3 font-bold uppercase">Total Tokens</th>
                    <th className="px-4 py-3 font-bold uppercase">Completed</th>
                    <th className="px-4 py-3 font-bold uppercase">Avg Wait (min)</th>
                    <th className="px-4 py-3 font-bold uppercase">Avg Service (min)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {analytics?.serviceBreakdown?.map((s, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/50 dark:hover:bg-slate-850/50 transition">
                      <td className="px-4 py-3 font-semibold text-slate-900 dark:text-white">{s.serviceName}</td>
                      <td className="px-4 py-3 font-mono">{s.total}</td>
                      <td className="px-4 py-3 font-mono text-emerald-600 dark:text-emerald-400">{s.completed}</td>
                      <td className="px-4 py-3 font-mono">{s.avgWaitMin}m</td>
                      <td className="px-4 py-3 font-mono">{s.avgServiceMin}m</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Departments & Services */}
      {activeTab === 'departments' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">
              Service Departments & Catalogs
            </h2>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setIsDeptModalOpen(true)}
                className="px-3.5 py-2 text-xs font-bold rounded-xl bg-brand-600 hover:bg-brand-700 text-white shadow-sm flex items-center gap-1.5 transition"
              >
                <Plus className="w-4 h-4" />
                <span>Add Department</span>
              </button>
              <button
                onClick={() => setIsServiceModalOpen(true)}
                className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-900 dark:bg-slate-100 text-white dark:text-slate-900 hover:opacity-90 transition flex items-center gap-1.5"
              >
                <Plus className="w-4 h-4" />
                <span>Add Service</span>
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {departments.map(dept => {
              const deptServices = services.filter(s => s.departmentId === dept.id);
              return (
                <div
                  key={dept.id}
                  className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-3xl p-6 shadow-sm"
                >
                  <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                    <div>
                      <span className="text-[10px] font-mono font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                        {dept.code}
                      </span>
                      <h3 className="text-base font-bold text-slate-900 dark:text-white mt-1">
                        {dept.name}
                      </h3>
                    </div>
                    <span className="text-xs text-slate-400 font-medium">
                      {deptServices.length} Services
                    </span>
                  </div>

                  <div className="mt-4 space-y-2">
                    {deptServices.map(svc => (
                      <div
                        key={svc.id}
                        className="p-3 rounded-xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/60 dark:border-slate-800/80 flex items-center justify-between text-xs"
                      >
                        <div>
                          <div className="font-bold text-slate-900 dark:text-white flex items-center gap-2">
                            <span>{svc.name}</span>
                            <span className="font-mono text-[10px] text-brand-600 dark:text-brand-400">
                              [{svc.prefix}]
                            </span>
                          </div>
                          <div className="text-[11px] text-slate-500 mt-0.5">
                            Est. {svc.estimatedDurationMinutes} mins • Priority Lvl {svc.priorityLevel}
                          </div>
                        </div>
                        <span className="font-mono font-bold text-slate-700 dark:text-slate-300">
                          {svc.currentWaitingCount || 0} waiting
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Tab 3: Service Counters */}
      {activeTab === 'counters' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">
              Service Counter Desks
            </h2>
            <button
              onClick={() => setIsCounterModalOpen(true)}
              className="px-3.5 py-2 text-xs font-bold rounded-xl bg-brand-600 hover:bg-brand-700 text-white shadow-sm flex items-center gap-1.5 transition"
            >
              <Plus className="w-4 h-4" />
              <span>Add Counter</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {counters.map(c => (
              <div
                key={c.id}
                className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-3xl p-6 shadow-sm flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-xs font-extrabold uppercase px-2.5 py-1 rounded-lg bg-brand-100 dark:bg-brand-950/60 text-brand-800 dark:text-brand-300 border border-brand-200 dark:border-brand-800">
                      {c.counterNumber}
                    </span>
                    <span className="text-xs text-slate-400">
                      {c.departmentName}
                    </span>
                  </div>

                  <h3 className="text-base font-bold text-slate-900 dark:text-white">
                    {c.name}
                  </h3>

                  <div className="mt-4 p-3 rounded-xl bg-slate-50 dark:bg-slate-950/50 border border-slate-200/60 dark:border-slate-800 text-xs">
                    <span className="text-slate-400 block text-[11px]">Assigned Operator:</span>
                    <span className="font-bold text-slate-800 dark:text-slate-200">
                      {c.currentStaffName ? c.currentStaffName : 'Unassigned / Vacant'}
                    </span>
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                  {c.currentStaffId ? (
                    <button
                      onClick={() => handleUnassignStaff(c.id)}
                      className="text-xs font-semibold text-rose-600 hover:underline"
                    >
                      Unassign Staff
                    </button>
                  ) : (
                    <button
                      onClick={() => {
                        setAssignCounterId(c.id.toString());
                        setIsAssignModalOpen(true);
                      }}
                      className="text-xs font-bold text-brand-600 dark:text-brand-400 hover:underline"
                    >
                      Assign Staff Member
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab 4: Staff Management */}
      {activeTab === 'staff' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">
              Organization Staff Directory
            </h2>
            <button
              onClick={() => setIsStaffModalOpen(true)}
              className="px-3.5 py-2 text-xs font-bold rounded-xl bg-brand-600 hover:bg-brand-700 text-white shadow-sm flex items-center gap-1.5 transition"
            >
              <Plus className="w-4 h-4" />
              <span>Add Staff Member</span>
            </button>
          </div>

          <div className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-3xl overflow-hidden shadow-sm">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-3.5 font-bold uppercase">Name</th>
                  <th className="px-5 py-3.5 font-bold uppercase">Email</th>
                  <th className="px-5 py-3.5 font-bold uppercase">Phone</th>
                  <th className="px-5 py-3.5 font-bold uppercase">Status</th>
                  <th className="px-5 py-3.5 font-bold uppercase">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                {staffList.map(st => (
                  <tr key={st.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-850/50 transition">
                    <td className="px-5 py-3.5 font-bold text-slate-900 dark:text-white">
                      {st.fullName}
                    </td>
                    <td className="px-5 py-3.5 text-slate-600 dark:text-slate-400 font-mono">
                      {st.email}
                    </td>
                    <td className="px-5 py-3.5 text-slate-500">
                      {st.phoneNumber || '-'}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`px-2 py-0.5 rounded text-[11px] font-bold ${
                        st.active
                          ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
                          : 'bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300'
                      }`}>
                        {st.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <button
                        onClick={async () => {
                          await api.patch(`/admin/users/${st.id}/toggle-status`);
                          loadDashboardData();
                        }}
                        className="text-xs font-semibold text-brand-600 dark:text-brand-400 hover:underline"
                      >
                        {st.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add Department Modal */}
      <Modal isOpen={isDeptModalOpen} onClose={() => setIsDeptModalOpen(false)} title="Create New Department">
        <form onSubmit={handleCreateDepartment} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1">Department Name</label>
            <input
              type="text"
              required
              value={deptName}
              onChange={(e) => setDeptName(e.target.value)}
              placeholder="e.g. Oncology Outpatient Clinic"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Department Code</label>
            <input
              type="text"
              required
              value={deptCode}
              onChange={(e) => setDeptCode(e.target.value)}
              placeholder="e.g. ONC"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Description</label>
            <textarea
              rows={2}
              value={deptDesc}
              onChange={(e) => setDeptDesc(e.target.value)}
              placeholder="Primary diagnostic and clinical review"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsDeptModalOpen(false)} className="px-4 py-2 text-xs">Cancel</button>
            <button type="submit" className="px-5 py-2 text-xs font-bold text-white bg-brand-600 rounded-xl">Create Department</button>
          </div>
        </form>
      </Modal>

      {/* Add Service Modal */}
      <Modal isOpen={isServiceModalOpen} onClose={() => setIsServiceModalOpen(false)} title="Add Service to Department">
        <form onSubmit={handleCreateService} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1">Department</label>
            <select
              value={targetDeptId}
              onChange={(e) => setTargetDeptId(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            >
              {departments.map(d => (
                <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold mb-1">Service Name</label>
              <input
                type="text"
                required
                value={serviceName}
                onChange={(e) => setServiceName(e.target.value)}
                placeholder="Chemotherapy Consultation"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Service Code</label>
              <input
                type="text"
                required
                value={serviceCode}
                onChange={(e) => setServiceCode(e.target.value)}
                placeholder="CHEMO"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold mb-1">Token Prefix</label>
              <input
                type="text"
                required
                value={servicePrefix}
                onChange={(e) => setServicePrefix(e.target.value)}
                placeholder="CH"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Est. Duration (m)</label>
              <input
                type="number"
                min={1}
                value={serviceDuration}
                onChange={(e) => setServiceDuration(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Priority Weight</label>
              <input
                type="number"
                min={1}
                max={5}
                value={servicePriority}
                onChange={(e) => setServicePriority(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Description</label>
            <textarea
              rows={2}
              value={serviceDesc}
              onChange={(e) => setServiceDesc(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsServiceModalOpen(false)} className="px-4 py-2 text-xs">Cancel</button>
            <button type="submit" className="px-5 py-2 text-xs font-bold text-white bg-brand-600 rounded-xl">Save Service</button>
          </div>
        </form>
      </Modal>

      {/* Add Counter Modal */}
      <Modal isOpen={isCounterModalOpen} onClose={() => setIsCounterModalOpen(false)} title="Create Service Counter Desk">
        <form onSubmit={handleCreateCounter} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1">Department</label>
            <select
              value={counterDeptId}
              onChange={(e) => setCounterDeptId(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            >
              {departments.map(d => (
                <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Counter Number / Code</label>
            <input
              type="text"
              required
              value={counterNumber}
              onChange={(e) => setCounterNumber(e.target.value)}
              placeholder="e.g. Counter 4 or Window C"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Counter Name / Location</label>
            <input
              type="text"
              required
              value={counterName}
              onChange={(e) => setCounterName(e.target.value)}
              placeholder="e.g. Room 104 - Express Care"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsCounterModalOpen(false)} className="px-4 py-2 text-xs">Cancel</button>
            <button type="submit" className="px-5 py-2 text-xs font-bold text-white bg-brand-600 rounded-xl">Create Counter</button>
          </div>
        </form>
      </Modal>

      {/* Assign Staff Modal */}
      <Modal isOpen={isAssignModalOpen} onClose={() => setIsAssignModalOpen(false)} title="Assign Staff Operator to Counter">
        <form onSubmit={handleAssignStaff} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1">Select Staff Member</label>
            <select
              required
              value={assignStaffId}
              onChange={(e) => setAssignStaffId(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            >
              <option value="">-- Choose Staff --</option>
              {staffList.filter(s => s.active).map(st => (
                <option key={st.id} value={st.id}>
                  {st.fullName} ({st.email})
                </option>
              ))}
            </select>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsAssignModalOpen(false)} className="px-4 py-2 text-xs">Cancel</button>
            <button type="submit" className="px-5 py-2 text-xs font-bold text-white bg-brand-600 rounded-xl">Confirm Assignment</button>
          </div>
        </form>
      </Modal>

      {/* Add Staff Modal */}
      <Modal isOpen={isStaffModalOpen} onClose={() => setIsStaffModalOpen(false)} title="Add New Staff Member">
        <form onSubmit={handleCreateStaff} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1">Full Name</label>
            <input
              type="text"
              required
              value={newStaffName}
              onChange={(e) => setNewStaffName(e.target.value)}
              placeholder="e.g. Nurse Michael Scott"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Email Address</label>
            <input
              type="email"
              required
              value={newStaffEmail}
              onChange={(e) => setNewStaffEmail(e.target.value)}
              placeholder="staff.mscott@hospital.com"
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold mb-1">Temporary Password</label>
              <input
                type="password"
                required
                minLength={6}
                value={newStaffPass}
                onChange={(e) => setNewStaffPass(e.target.value)}
                placeholder="staff123"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Phone (Optional)</label>
              <input
                type="tel"
                value={newStaffPhone}
                onChange={(e) => setNewStaffPhone(e.target.value)}
                placeholder="+1-555-0299"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-xs"
              />
            </div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsStaffModalOpen(false)} className="px-4 py-2 text-xs">Cancel</button>
            <button type="submit" className="px-5 py-2 text-xs font-bold text-white bg-brand-600 rounded-xl">Create Account</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
