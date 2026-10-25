import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, ParkingSquare, LogIn, ListChecks, BarChart3, CarFront } from 'lucide-react';

const LINKS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/slots', label: 'Parking Slots', icon: ParkingSquare },
  { to: '/entry', label: 'Vehicle Entry', icon: LogIn },
  { to: '/bookings', label: 'Active Bookings', icon: ListChecks },
  { to: '/reports', label: 'Revenue Reports', icon: BarChart3 },
];

export default function MainLayout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <CarFront size={26} />
          <span>Smart Parking</span>
        </div>
        <nav>
          {LINKS.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Icon size={19} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
