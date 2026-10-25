import { useState } from 'react';
import { Plus, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import SlotCard from '../components/SlotCard.jsx';
import Loader from '../components/Loader.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { useToast } from '../components/ToastProvider.jsx';
import useFetch from '../hooks/useFetch.js';
import { getErrorMessage, slotApi } from '../services/api.js';

export default function ParkingSlots() {
  const toast = useToast();
  const { data: slots, loading, error, reload } = useFetch(slotApi.getAll);
  const [slotNumber, setSlotNumber] = useState('');
  const [saving, setSaving] = useState(false);

  const available = slots?.filter((s) => s.status === 'AVAILABLE').length ?? 0;

  const handleCreate = async (event) => {
    event.preventDefault();
    if (saving) return;
    const value = slotNumber.trim();
    if (!value) return toast.error('Please enter a slot number.');
    if (!/^[A-Za-z0-9-]+$/.test(value)) return toast.error('Slot number can contain only letters, digits and hyphens.');

    setSaving(true);
    try {
      const created = await slotApi.create(value);
      toast.success(`Slot ${created.slotNumber} created.`);
      setSlotNumber('');
      await reload();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Parking Slots"
        subtitle={slots ? `${available} of ${slots.length} slots available` : 'All parking slots'}
        actions={<button className="btn btn-outline" onClick={reload} disabled={loading}><RefreshCw size={16} /> Refresh</button>}
      />

      <form className="card inline-form" onSubmit={handleCreate}>
        <label className="field grow">
          New slot number
          <input value={slotNumber} onChange={(e) => setSlotNumber(e.target.value)} placeholder="e.g. B-01" maxLength={20} />
        </label>
        <button className="btn btn-primary" type="submit" disabled={saving}>
          <Plus size={16} /> {saving ? 'Adding…' : 'Add slot'}
        </button>
      </form>

      <div className="legend">
        <span><i className="dot dot-green" /> Available</span>
        <span><i className="dot dot-red" /> Occupied</span>
      </div>

      {loading && !slots && <Loader />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {slots && slots.length === 0 && <EmptyState title="No parking slots yet" hint="Add your first slot using the form above." />}
      {slots && slots.length > 0 && (
        <div className="slot-grid">
          {slots.map((slot) => <SlotCard key={slot.id} slot={slot} />)}
        </div>
      )}
    </>
  );
}
