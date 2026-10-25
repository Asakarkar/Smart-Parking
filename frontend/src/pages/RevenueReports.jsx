import { useState } from 'react';
import { IndianRupee, ReceiptText } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import StatCard from '../components/StatCard.jsx';
import Loader from '../components/Loader.jsx';
import ErrorState from '../components/ErrorState.jsx';
import useFetch from '../hooks/useFetch.js';
import { reportApi } from '../services/api.js';
import { formatCurrency, formatDateLong, todayIso } from '../utils/format.js';

export default function RevenueReports() {
  const [date, setDate] = useState(todayIso());
  const { data, loading, error, reload } = useFetch(() => reportApi.getRevenue(date), [date]);

  return (
    <>
      <PageHeader title="Revenue Reports" subtitle="Collected revenue counts PAID bookings only" />

      <div className="card inline-form">
        <label className="field">
          Select date
          <input type="date" value={date} onChange={(e) => e.target.value && setDate(e.target.value)} />
        </label>
      </div>

      {loading && !data && <Loader />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && !error && (
        <>
          <h2 className="section-title">{formatDateLong(data.date)}</h2>
          <div className="stat-grid two">
            <StatCard icon={IndianRupee} label="Total collected revenue" value={formatCurrency(data.totalRevenue)} tone="green" />
            <StatCard icon={ReceiptText} label="Paid bookings" value={data.paidBookings} tone="blue" />
          </div>
          {data.paidBookings === 0 && <p className="muted">No payments were recorded on this date.</p>}
          <p className="muted small">Dates are in {data.timezone} time.</p>
        </>
      )}
    </>
  );
}
