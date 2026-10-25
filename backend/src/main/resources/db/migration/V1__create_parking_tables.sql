CREATE TABLE parking_slots (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slot_number VARCHAR(20)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_parking_slots_slot_number UNIQUE (slot_number),
    CONSTRAINT ck_parking_slots_status CHECK (status IN ('AVAILABLE', 'OCCUPIED'))
);

CREATE TABLE parking_bookings (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_number VARCHAR(20)   NOT NULL,
    slot_id        BIGINT        NOT NULL REFERENCES parking_slots (id),
    entry_time     TIMESTAMPTZ   NOT NULL,
    exit_time      TIMESTAMPTZ,
    parking_fee    NUMERIC(10,2),
    booking_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    payment_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at        TIMESTAMPTZ,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_bookings_booking_status CHECK (booking_status IN ('ACTIVE', 'COMPLETED')),
    CONSTRAINT ck_bookings_payment_status CHECK (payment_status IN ('PENDING', 'PAID')),
    CONSTRAINT ck_bookings_exit_after_entry CHECK (exit_time IS NULL OR exit_time >= entry_time)
);

-- A slot can have only ONE active booking, and a vehicle can have only ONE active booking.
CREATE UNIQUE INDEX uq_active_booking_per_slot    ON parking_bookings (slot_id)        WHERE booking_status = 'ACTIVE';
CREATE UNIQUE INDEX uq_active_booking_per_vehicle ON parking_bookings (vehicle_number) WHERE booking_status = 'ACTIVE';

CREATE INDEX idx_bookings_status          ON parking_bookings (booking_status);
CREATE INDEX idx_bookings_payment_paid_at ON parking_bookings (payment_status, paid_at);
