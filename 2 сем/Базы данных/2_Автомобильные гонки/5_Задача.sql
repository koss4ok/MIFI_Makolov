-- Определить, какие классы автомобилей имеют наибольшее количество автомобилей с низкой средней позицией (больше 3.0)
-- и вывести информацию о каждом автомобиле из этих классов, включая его имя, класс, среднюю позицию, количество гонок, в которых он участвовал,
-- страну производства класса автомобиля, а также общее количество гонок для каждого класса. 
-- Отсортировать результаты по количеству автомобилей с низкой средней позицией.

WITH car_stats AS (
    SELECT cars.name AS car, cars.class, AVG(results.position) AS avg_position, COUNT(*) AS race_count
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.name, cars.class
),
low_per_class AS (
    SELECT class, COUNT(*) AS low_count
    FROM car_stats
    GROUP BY class
    HAVING MIN(avg_position) >= 3.0
),
class_race_count AS (
    SELECT cars.class, COUNT(*) AS total_races
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.class
),
ranked_cars AS (
    SELECT car_stats.*,
        ROW_NUMBER() OVER (
            PARTITION BY car_stats.class
            ORDER BY car_stats.avg_position DESC, car_stats.car
        ) AS rn
    FROM car_stats
    JOIN low_per_class ON car_stats.class = low_per_class.class
)
SELECT ranked_cars.car, ranked_cars.class, ROUND(ranked_cars.avg_position, 4) AS avg_position,
    ranked_cars.race_count, classes.country, class_race_count.total_races, low_per_class.low_count
FROM ranked_cars
JOIN classes ON ranked_cars.class = classes.class
JOIN class_race_count ON ranked_cars.class = class_race_count.class
JOIN low_per_class ON ranked_cars.class = low_per_class.class
WHERE ranked_cars.rn = 1
ORDER BY low_per_class.low_count DESC, ranked_cars.class
	