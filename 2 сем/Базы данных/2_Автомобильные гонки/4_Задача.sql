-- Определить, какие автомобили имеют среднюю позицию лучше (меньше) средней позиции всех автомобилей в своем классе 
-- (то есть автомобилей в классе должно быть минимум два, чтобы выбрать один из них).
-- Вывести информацию об этих автомобилях, включая их имя, класс, среднюю позицию, количество гонок, в которых они участвовали,и страну производства класса автомобиля. 
-- Также отсортировать результаты по классу и затем по средней позиции в порядке возрастания.

WITH car_stats AS (
    SELECT cars.name AS car, cars.class, AVG(results.position) AS avg_position, COUNT(*) AS race_count
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.name, cars.class
),
class_stats AS (
    SELECT cars.class, AVG(results.position) AS class_avg_position, COUNT(DISTINCT cars.name) AS car_count
    FROM cars
    JOIN results ON cars.name = results.car
    GROUP BY cars.class
)
SELECT car_stats.car, car_stats.class, car_stats.avg_position, car_stats.race_count, classes.country
FROM car_stats
JOIN class_stats ON car_stats.class = class_stats.class
JOIN classes ON car_stats.class = classes.class
WHERE car_stats.avg_position < class_stats.class_avg_position AND class_stats.car_count >= 2
ORDER BY car_stats.class, car_stats.avg_position