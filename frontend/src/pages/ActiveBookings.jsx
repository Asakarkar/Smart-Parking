import { useState } from 'react';
import { Calculator, LogOut, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import Loader from '../components/Loader.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import Receipt from '../components/Receipt.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../components/ToastProvider.jsx';
import useFetch from '../hooks/useFetch.js';
import { bookingApi, getErrorMessage } from '../services/api.js';
import { formatCurrency, formatDateTime, formatDuration } from '../utils/format.js';

export default function ActiveBookings() {
  const toast = useToast();
  const active = useFetch(bookingApi.getActive);
  const pending = useFetch(() => bookingApi.getCompleted('PENDING'));
  const [busyId, setBusyId] = useState(null); // booking being checked out / priced / paid
  const [receipt, setReceipt] = useState(null);

  const refreshAll = () => Promise.all([active.reload(), pending.reload()]);

  const handleFare = async (booking) => {
    if (busyId) return;
    setBusyId(booking.id);
    try {
      const fare = await bookingApi.getFare(booking.id);
      active.setData((rows) =>
        rows.map((row) =>
          row.id === booking.id ? { ...row, parkingFee: fare.estimatedFee, durationSeconds: fare.durationSeconds } : row
        )
      );
      toast.success(`Current fare for ${booking.vehicleNumber}: ${formatCurrency(fare.estimatedFee)}`);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  };

  const handleCheckout = async (booking) => {
    if (busyId) return;
    setBusyId(booking.id);
    try {
      const completed = await bookingApi.checkout(booking.id);
      setReceipt(completed);
      toast.success(`${completed.vehicleNumber} checked out. Fee: ${formatCurrency(completed.parkingFee)}`);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setBusyId(null);
      refreshAll();
    }
  };

  const handleMarkPaid = async (id) => {
    try {
      const paid = await bookingApi.markAsPaid(id);
      toast.success(`Payment of ${formatCurrency(paid.parkingFee)} recorded for booking #${paid.id}.`);
      setReceipt((current) => (current && current.id === id ? paid : current));
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      pending.reload();
    }
  };

  return (
    <>
      <PageHeader
        title="Active Bookings"
        subtitle="Vehicles currently parked. Fees are estimates until checkout."
        actions={
          <button className="btn btn-outline" onClick={refreshAll} disabled={active.loading || pending.loading}>
            <RefreshCw size={16} /> Refresh
          </button>
        }
      />

      <div className="card table-card">
        {active.loading && !active.data && <Loader />}
        {active.error && <ErrorState message={active.error} onRetry={active.reload} />}
        {active.data && active.data.length === 0 && (
          <EmptyState title="No vehicles are parked" hint="Use Vehicle Entry to register one." />
        )}
        {active.data && active.data.length > 0 && (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Booking ID</th><th>Vehicle</th><th>Slot</th><th>Entry time</th>
                  <th>Duration</th><th>Estimated fee</th><th className="right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {active.data.map((b) => (
                  <tr key={b.id}>
                    <td>#{b.id}</td>
                    <td><strong>{b.vehicleNumber}</strong></td>
                    <td>{b.slotNumber}</td>
                    <td>{formatDateTime(b.entryTime)}</td>
                    <td>{formatDuration(b.durationSeconds)}</td>
                    <td>{formatCurrency(b.parkingFee)}</td>
                    <td className="right actions">
                      <button className="btn btn-outline btn-sm" onClick={() => handleFare(b)} disabled={busyId !== null}>
                        <Calculator size={14} /> Fare
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleCheckout(b)} disabled={busyId !== null}>
                        <LogOut size={14} /> {busyId === b.id ? 'Working…' : 'Checkout'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <h2 className="section-title">Awaiting payment</h2>
      <div className="card table-card">
        {pending.loading && !pending.data && <Loader />}
        {pending.error && <ErrorState message={pending.error} onRetry={pending.reload} />}
        {pending.data && pending.data.length === 0 && <EmptyState title="No pending payments" />}
        {pending.data && pending.data.length > 0 && (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Booking ID</th><th>Vehicle</th><th>Slot</th><th>Exit time</th>
                  <th>Final fee</th><th>Payment</th><th className="right">Action</th>
                </tr>
              </thead>
              <tbody>
                {pending.data.map((b) => (
                  <tr key={b.id}>
                    <td>#{b.id}</td>
                    <td><strong>{b.vehicleNumber}</strong></td>
                    <td>{b.slotNumber}</td>
                    <td>{formatDateTime(b.exitTime)}</td>
                    <td>{formatCurrency(b.parkingFee)}</td>
                    <td><StatusBadge status={b.paymentStatus} /></td>
                    <td className="right">
                      <button className="btn btn-primary btn-sm" onClick={() => handleMarkPaid(b.id)}>Mark as Paid</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {receipt && <Receipt booking={receipt} onMarkPaid={handleMarkPaid} onClose={() => setReceipt(null)} />}
    </>
  );
}
