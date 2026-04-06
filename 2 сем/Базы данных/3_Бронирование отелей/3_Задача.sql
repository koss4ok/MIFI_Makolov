-- Задача 3: Условия задачи (комментарии)
-- Определить клиентов, сделавших более двух бронирований в разных отелях
-- Вывести: ID_customer, имя, email, телефон, общее количество бронирований, список отелей, среднее пребывание в днях

SELECT c.ID_customer, c.name, c.email, c.phone,
       COUNT(*) AS total_bookings,
       STRING_AGG(DISTINCT h.name, ', ' ORDER BY h.name) AS hotels,
       AVG(b.check_out_date - b.check_in_date) AS avg_stay_days
FROM Booking b
JOIN Room ro ON ro.ID_room = b.ID_room
JOIN Hotel h ON h.ID_hotel = ro.ID_hotel
JOIN Customer c ON c.ID_customer = b.ID_customer
GROUP BY c.ID_customer, c.name, c.email, c.phone
HAVING COUNT(*) > 2 AND COUNT(DISTINCT h.ID_hotel) > 1
ORDER BY total_bookings DESC;
