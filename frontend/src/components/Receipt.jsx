import { useState } from 'react';
import { CheckCircle2 } from 'lucide-react';
import StatusBadge from './StatusBadge.jsx';
import { formatCurrency, formatDateTime, formatDuration } from '../utils/format.js';

/** Popup shown after checkout: final fee, transaction details and the Mark as Paid action. */
export default function Receipt({ booking, onMarkPaid, onClose }) {
  const [paying, setPaying] = useState(false);
  const isPaid = booking.paymentStatus === 'PAID';

  const handlePay = async () => {
    setPaying(true);
    try {
      await onMarkPaid(booking.id);
    } finally {
      setPaying(false);
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal card">
        <div className="modal-title">
          <CheckCircle2 size={26} className="text-green" />
          <h2>Checkout complete</h2>
        </div>

        <p className="receipt-fee">{formatCurrency(booking.parkingFee)}</p>

        <dl className="details">
          <dt>Transaction (Booking ID)</dt><dd>#{booking.id}</dd>
          <dt>Vehicle</dt><dd>{booking.vehicleNumber}</dd>
          <dt>Slot</dt><dd>{booking.slotNumber}</dd>
          <dt>Entry</dt><dd>{formatDateTime(booking.entryTime)}</dd>
          <dt>Exit</dt><dd>{formatDateTime(booking.exitTime)}</dd>
          <dt>Duration</dt><dd>{formatDuration(booking.durationSeconds)}</dd>
          <dt>Payment</dt><dd><StatusBadge status={booking.paymentStatus} /></dd>
        </dl>

        <div className="modal-actions">
          {!isPaid && (
            <button className="btn btn-primary" onClick={handlePay} disabled={paying}>
              {paying ? 'Saving…' : 'Mark as Paid'}
            </button>
          )}
          <button className="btn btn-outline" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}
