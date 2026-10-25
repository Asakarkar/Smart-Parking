const TONES = {
  AVAILABLE: 'green', OCCUPIED: 'red', ACTIVE: 'blue', COMPLETED: 'gray', PAID: 'green', PENDING: 'amber',
};

export default function StatusBadge({ status }) {
  return <span className={`badge badge-${TONES[status] || 'gray'}`}>{status}</span>;
}
