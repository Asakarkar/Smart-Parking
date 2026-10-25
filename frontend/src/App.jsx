import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ParkingSlots from './pages/ParkingSlots.jsx';
import VehicleEntry from './pages/VehicleEntry.jsx';
import ActiveBookings from './pages/ActiveBookings.jsx';
import RevenueReports from './pages/RevenueReports.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="slots" element={<ParkingSlots />} />
        <Route path="entry" element={<VehicleEntry />} />
        <Route path="bookings" element={<ActiveBookings />} />
        <Route path="reports" element={<RevenueReports />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
