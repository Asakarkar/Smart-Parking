import { Car, CircleParking } from 'lucide-react';

export default function SlotCard({ slot }) {
  const occupied = slot.status === 'OCCUPIED';
  return (
    <div className={`slot-card ${occupied ? 'slot-occupied' : 'slot-available'}`}>
      <div className="slot-top">
        <span className="slot-number">{slot.slotNumber}</span>
        {occupied ? <Car size={20} /> : <CircleParking size={20} />}
      </div>
      <p className="slot-status">{occupied ? 'Occupied' : 'Available'}</p>
      <p className="slot-vehicle">{occupied ? slot.vehicleNumber : 'Free to use'}</p>
    </div>
  );
}
