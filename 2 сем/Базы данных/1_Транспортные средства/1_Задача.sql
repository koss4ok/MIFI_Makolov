-- Найдите производителей (maker) и модели всех мотоциклов,
-- которые имеют мощность более 150 лошадиных сил, стоят менее 20 тысяч долларов и являются спортивными (тип Sport). 
-- Также отсортируйте результаты по мощности в порядке убывания.

SELECT maker, motorcycle.model
FROM motorcycle
JOIN vehicle on motorcycle.model = vehicle.model
WHERE horsepower  150 AND price  20000 and motorcycle.type = 'Sport'