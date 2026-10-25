import { useState } from 'react';
import { ParkingSquare, CircleCheck, CircleX, ClipboardList, IndianRupee, Gauge, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import StatCard from '../components/StatCard.jsx';
import Loader from '../components/Loader.jsx';
import ErrorState from '../components/ErrorState.jsx';
import useFetch from '../hooks/useFetch.js';
import { reportApi } from '../services/api.js';
import { formatCurrency, formatDateLong, todayIso } from '../utils/format.js';

export default function Dashboard() {
  const [date, setDate] = useState(todayIso());
  const { data, loading, error, reload } = useFetch(() => reportApi.getDashboard(date), [date]);

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle="Live parking summary from the database"
        actions={
          <>
            <label className="inline-field">
              Revenue date
              <input type="date" value={date} max={todayIso()} onChange={(e) => e.target.value && setDate(e.target.value)} />
            </label>
            <button className="btn btn-outline" onClick={reload} disabled={loading}>
              <RefreshCw size={16} /> Refresh
            </button>
          </>
        }
      />

      {loading && !data && <Loader />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && !error && (
        <>
          <div className="stat-grid">
            <StatCard icon={ParkingSquare} label="Total slots" value={data.totalSlots} />
            <StatCard icon={CircleCheck} label="Available slots" value={data.availableSlots} tone="green" />
            <StatCard icon={CircleX} label="Occupied slots" value={data.occupiedSlots} tone="red" />
            <StatCard icon={ClipboardList} label="Active bookings" value={data.activeBookings} tone="blue" />
            <StatCard icon={IndianRupee} label={`Revenue · ${formatDateLong(data.revenueDate)}`} value={formatCurrency(data.revenue)} tone="green" />
            <StatCard icon={Gauge} label="Occupancy" value={`${data.occupancyPercentage}%`} tone="amber" />
          </div>

          <div className="card occupancy-card">
            <div className="row-between">
              <strong>Occupancy</strong>
              <span className="muted">{data.occupiedSlots} of {data.totalSlots} slots in use</span>
            </div>
            <div className="progress">
              <div className="progress-bar" style={{ width: `${Math.min(100, Number(data.occupancyPercentage))}%` }} />
            </div>
          </div>
        </>
      )}
    </>
  );
}
