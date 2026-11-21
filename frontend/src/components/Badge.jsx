import React from 'react';

export const StatusBadge = ({ status }) => {
  const configs = {
    WAITING: {
      label: 'Waiting',
      bg: 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 border-amber-200 dark:border-amber-800/50',
      dot: 'bg-amber-500'
    },
    CALLED: {
      label: 'Now Calling',
      bg: 'bg-blue-100 text-blue-800 dark:bg-blue-950/60 dark:text-blue-300 border-blue-200 dark:border-blue-800/50 animate-pulse',
      dot: 'bg-blue-500 animate-ping'
    },
    SERVING: {
      label: 'Serving',
      bg: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800/50',
      dot: 'bg-emerald-500'
    },
    COMPLETED: {
      label: 'Completed',
      bg: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border-slate-200 dark:border-slate-700',
      dot: 'bg-slate-400'
    },
    CANCELLED: {
      label: 'Cancelled',
      bg: 'bg-rose-100 text-rose-800 dark:bg-rose-950/60 dark:text-rose-300 border-rose-200 dark:border-rose-800/50',
      dot: 'bg-rose-500'
    },
    NO_SHOW: {
      label: 'No Show',
      bg: 'bg-purple-100 text-purple-800 dark:bg-purple-950/60 dark:text-purple-300 border-purple-200 dark:border-purple-800/50',
      dot: 'bg-purple-500'
    }
  };

  const config = configs[status] || {
    label: status,
    bg: 'bg-slate-100 text-slate-700 border-slate-200',
    dot: 'bg-slate-400'
  };

  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${config.bg}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
};

export const PriorityBadge = ({ priority }) => {
  if (priority === 'VIP') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold tracking-wide uppercase bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300 border border-purple-300 dark:border-purple-800">
        VIP
      </span>
    );
  }
  if (priority === 'PRIORITY') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold tracking-wide uppercase bg-sky-100 text-sky-800 dark:bg-sky-900/40 dark:text-sky-300 border border-sky-300 dark:border-sky-800">
        Priority
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium text-slate-500 dark:text-slate-400">
      Regular
    </span>
  );
};
