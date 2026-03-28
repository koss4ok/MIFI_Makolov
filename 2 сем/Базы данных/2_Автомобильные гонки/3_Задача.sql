-- Определить классы автомобилей, которые имеют наименьшую среднюю позицию в гонках, и вывести информацию о каждом автомобиле из этих классов,
-- включая его имя, среднюю позицию, количество гонок, в которых он участвовал, страну производства класса автомобиля, 
-- а также общее количество гонок, в которых участвовали автомобили этих классов. 
-- Если несколько классов имеют одинаковую среднюю позицию, выбрать все из них.

WITH class_stats AS (
    SELECT cars.class, AVG(results.position) AS class_avg_position
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.class
),
best_classes AS (
    SELECT class
    FROM class_stats
    WHERE class_avg_position = (
        SELECT MIN(class_avg_position)
        FROM class_stats
    )
),
car_stats AS (
    SELECT cars.class, cars.name AS car, AVG(results.position) AS avg_position, COUNT(*) AS race_count
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.class, cars.name
),
class_race_count AS (
    SELECT cars.class, COUNT(*) AS total_races
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.class
)
SELECT car_stats.car, car_stats.avg_position, car_stats.race_count, classes.country, class_race_count.total_races
FROM car_stats
JOIN best_classes ON car_stats.class = best_classes.class
JOIN classes ON car_stats.class = classes.class
JOIN class_race_count ON car_stats.class = class_race_count.class
ORDER BY car_stats.avg_position