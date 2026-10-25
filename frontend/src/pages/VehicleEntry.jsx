import { useState } from 'react';
import { LogIn } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import Loader from '../components/Loader.jsx';
import ErrorState from '../components/ErrorState.jsx';
import { useToast } from '../components/ToastProvider.jsx';
import useFetch from '../hooks/useFetch.js';
import { bookingApi, getErrorMessage, slotApi } from '../services/api.js';
import { VEHICLE_NUMBER_PATTERN, formatDateTime } from '../utils/format.js';

export default function VehicleEntry() {
  const toast = useToast();
  const { data: slots, loading, error, reload } = useFetch(slotApi.getAvailable);
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [slotId, setSlotId] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [lastBooking, setLastBooking] = useState(null);

  const validate = () => {
    const errors = {};
    if (!vehicleNumber.trim()) errors.vehicleNumber = 'Vehicle number is required.';
    else if (!VEHICLE_NUMBER_PATTERN.test(vehicleNumber.trim())) errors.vehicleNumber = 'Use a format like MH31AB1234.';
    if (!slotId) errors.slotId = 'Please choose a parking slot.';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (submitting || !validate()) return;

    setSubmitting(true);
    try {
      const booking = await bookingApi.create(vehicleNumber.trim(), Number(slotId));
      toast.success(`Booking #${booking.id} created: ${booking.vehicleNumber} parked in ${booking.slotNumber}.`);
      setLastBooking(booking);
      setVehicleNumber('');
      setSlotId('');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setSubmitting(false);
      reload(); // always refresh the list: it may have changed (e.g. someone else took the slot)
    }
  };

  return (
    <>
      <PageHeader title="Vehicle Entry" subtitle="Register a vehicle and assign it an available slot" />

      {loading && !slots && <Loader />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {slots && !error && (
        <form className="card form-card" onSubmit={handleSubmit} noValidate>
          <label className="field">
            Vehicle registration number
            <input
              value={vehicleNumber}
              onChange={(e) => setVehicleNumber(e.target.value.toUpperCase())}
              placeholder="MH31AB1234"
              maxLength={20}
              className={fieldErrors.vehicleNumber ? 'invalid' : ''}
            />
            {fieldErrors.vehicleNumber && <span className="field-error">{fieldErrors.vehicleNumber}</span>}
          </label>

          <label className="field">
            Parking slot ({slots.length} available)
            <select value={slotId} onChange={(e) => setSlotId(e.target.value)} className={fieldErrors.slotId ? 'invalid' : ''}>
              <option value="">Select a slot…</option>
              {slots.map((slot) => <option key={slot.id} value={slot.id}>{slot.slotNumber}</option>)}
            </select>
            {fieldErrors.slotId && <span className="field-error">{fieldErrors.slotId}</span>}
          </label>

          {slots.length === 0 && <p className="field-error">All slots are occupied right now.</p>}

          <button className="btn btn-primary" type="submit" disabled={submitting || slots.length === 0}>
            <LogIn size={16} /> {submitting ? 'Saving…' : 'Register entry'}
          </button>
        </form>
      )}

      {lastBooking && (
        <div className="card success-card">
          <strong>Last booking saved</strong>
          <p>
            #{lastBooking.id} · {lastBooking.vehicleNumber} · Slot {lastBooking.slotNumber} · Entry {formatDateTime(lastBooking.entryTime)}
          </p>
        </div>
      )}
    </>
  );
}
