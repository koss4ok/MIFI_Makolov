-- Определить автомобиль, который имеет наименьшую среднюю позицию в гонках среди всех автомобилей,
-- и вывести информацию об этом автомобиле, включая его класс, среднюю позицию, количество гонок, в которых он участвовал, и страну производства класса автомобиля.
-- Если несколько автомобилей имеют одинаковую наименьшую среднюю позицию, выбрать один из них по алфавиту (по имени автомобиля).

SELECT cars.class, cars.name AS car, AVG(results.position) AS avg_position, COUNT(*) AS race_count, classes.country
FROM cars
JOIN results ON cars.name = results.car
JOIN classes ON cars.class = classes.class
GROUP BY cars.class, cars.name, classes.country
ORDER BY AVG(results.position), cars.name
LIMIT 1;