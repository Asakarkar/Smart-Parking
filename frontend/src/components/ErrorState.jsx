import { AlertTriangle } from 'lucide-react';

export default function ErrorState({ message, onRetry }) {
  return (
    <div className="state-box state-error">
      <AlertTriangle size={28} />
      <p>{message}</p>
      {onRetry && <button className="btn btn-outline" onClick={onRetry}>Try again</button>}
    </div>
  );
}
