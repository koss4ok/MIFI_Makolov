-- Заполнение данных для отелей
INSERT INTO Hotel (name, location) VALUES
('Grand Hotel','Paris, France'),
('Ocean View Resort','Miami, USA'),
('Mountain Retreat','Aspen, USA'),
('City Center Inn','New York, USA');

-- Заполнение данных для Room
INSERT INTO Room (ID_hotel, room_type, price, capacity) VALUES
(1,'Single',100,1),
(1,'Double',150,2),
(2,'Single',120,1),
(3,'Suite',300,4);

-- Заполнение данных для Customer
INSERT INTO Customer (name, email, phone) VALUES
('John Doe','john@example.com','+1111111111'),
('Jane Smith','jane@example.com','+2222222222');

-- Заполнение данных для Booking
INSERT INTO Booking (ID_room, ID_customer, check_in_date, check_out_date) VALUES
(1,1,'2026-01-01','2026-01-03'),
(2,2,'2026-02-01','2026-02-05');
