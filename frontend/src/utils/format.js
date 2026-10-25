// All dates are shown in Indian Standard Time so every page looks the same.
const TIME_ZONE = 'Asia/Kolkata';

const currencyFormatter = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

const dateTimeFormatter = new Intl.DateTimeFormat('en-IN', {
  timeZone: TIME_ZONE,
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: true,
});

export const formatCurrency = (amount) => currencyFormatter.format(Number(amount ?? 0));

export const formatDateTime = (value) => (value ? dateTimeFormatter.format(new Date(value)) : '—');

export function formatDuration(totalSeconds) {
  const seconds = Math.max(0, Number(totalSeconds ?? 0));
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours === 0 && minutes === 0) return seconds < 60 ? `${seconds}s` : '0m';
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
}

/** Today's date as yyyy-MM-dd in India time (what the date picker and the API expect). */
export const todayIso = () =>
  new Intl.DateTimeFormat('en-CA', { timeZone: TIME_ZONE }).format(new Date());

export const formatDateLong = (isoDate) =>
  isoDate
    ? new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: 'long', year: 'numeric', timeZone: 'UTC' }).format(
        new Date(`${isoDate}T00:00:00Z`)
      )
    : '';

export const VEHICLE_NUMBER_PATTERN = /^[A-Za-z]{2}[ -]?[0-9]{1,2}[ -]?[A-Za-z]{0,3}[ -]?[0-9]{4}$/;
