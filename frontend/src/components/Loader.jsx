export default function Loader({ text = 'Loading…' }) {
  return (
    <div className="state-box">
      <div className="spinner" />
      <p>{text}</p>
    </div>
  );
}
