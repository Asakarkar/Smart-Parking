export default function StatCard({ icon: Icon, label, value, tone = 'navy' }) {
  return (
    <div className="card stat-card">
      <div className={`stat-icon tone-${tone}`}><Icon size={22} /></div>
      <div>
        <p className="stat-label">{label}</p>
        <p className="stat-value">{value}</p>
      </div>
    </div>
  );
}
