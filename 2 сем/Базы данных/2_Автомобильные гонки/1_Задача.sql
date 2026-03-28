-- Определить, какие автомобили из каждого класса имеют наименьшую среднюю позицию в гонках, и 
-- вывести информацию о каждом таком автомобиле для данного класса, включая его класс, среднюю позицию и количество гонок, в которых он участвовал. 
-- Также отсортировать результаты по средней позиции.

SELECT outer_cars.class, outer_cars.name AS car, AVG(results.position) AS avg_position, COUNT(*) AS race_count
FROM cars AS outer_cars
JOIN results ON outer_cars.name = results.car
GROUP BY outer_cars.class, outer_cars.name
HAVING AVG(results.position) = (
    SELECT MIN(avg_position)
    FROM (
        SELECT inner_cars.name, AVG(inner_results.position) AS avg_position
        FROM cars AS inner_cars
        JOIN results AS inner_results ON inner_cars.name = inner_results.car
        WHERE inner_cars.class = outer_cars.class
        GROUP BY inner_cars.name
    ) AS subquery
)
ORDER BY avg_position;