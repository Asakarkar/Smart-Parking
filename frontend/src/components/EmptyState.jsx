import { Inbox } from 'lucide-react';

export default function EmptyState({ title, hint }) {
  return (
    <div className="state-box">
      <Inbox size={28} />
      <p><strong>{title}</strong></p>
      {hint && <p className="muted">{hint}</p>}
    </div>
  );
}
