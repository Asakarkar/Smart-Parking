import axios from 'axios';

// One shared Axios instance: every page talks to the backend through this file.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
});

/** Converts any Axios error into a short, readable message for the user. */
export function getErrorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.';
  if (error.request) return 'Cannot reach the server. Check that the backend is running on port 8080.';
  return 'Something went wrong. Please try again.';
}

const data = (promise) => promise.then((response) => response.data);

export const slotApi = {
  getAll: () => data(http.get('/slots')),
  getAvailable: () => data(http.get('/slots/available')),
  create: (slotNumber) => data(http.post('/slots', { slotNumber })),
};

export const bookingApi = {
  getActive: () => data(http.get('/bookings/active')),
  getCompleted: (paymentStatus) =>
    data(http.get('/bookings/completed', { params: paymentStatus ? { paymentStatus } : {} })),
  create: (vehicleNumber, slotId) => data(http.post('/bookings', { vehicleNumber, slotId })),
  getFare: (id) => data(http.get(`/bookings/${id}/fare`)),
  checkout: (id) => data(http.post(`/bookings/${id}/checkout`)),
  markAsPaid: (id) => data(http.patch(`/bookings/${id}/payment`)),
};

export const reportApi = {
  getRevenue: (date) => data(http.get('/reports/revenue', { params: { date } })),
  getDashboard: (date) => data(http.get('/dashboard', { params: { date } })),
};
