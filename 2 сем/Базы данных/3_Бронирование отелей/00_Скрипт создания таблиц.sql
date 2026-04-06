-- Скрипт создания таблиц для отелей (PostgreSQL вариация)

CREATE TABLE Hotel (
  ID_hotel SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL
);

CREATE TABLE Room (
  ID_room SERIAL PRIMARY KEY,
  ID_hotel INT NOT NULL,
  room_type VARCHAR(20) NOT NULL CHECK (room_type IN ('Single','Double','Suite')),
  price DECIMAL(10,2) NOT NULL,
  capacity INT NOT NULL,
  FOREIGN KEY (ID_hotel) REFERENCES Hotel(ID_hotel)
);

CREATE TABLE Customer (
  ID_customer SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  phone VARCHAR(20) NOT NULL
);

CREATE TABLE Booking (
  ID_booking SERIAL PRIMARY KEY,
  ID_room INT NOT NULL,
  ID_customer INT NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  FOREIGN KEY (ID_room) REFERENCES Room(ID_room),
  FOREIGN KEY (ID_customer) REFERENCES Customer(ID_customer)
);
