-- Задание 4.3
-- Все менеджеры с подчиненными + проекты/задачи и кол-во подчиненных (непрямых тоже)

WITH RECURSIVE manager_subtree AS (
  SELECT e.EmployeeID AS manager_id, e.EmployeeID AS employee_id
  FROM Employees e
  JOIN Roles r ON r.RoleID = e.RoleID
  WHERE r.RoleName = 'Менеджер'
  UNION ALL
  SELECT ms.manager_id, child.EmployeeID AS employee_id
  FROM manager_subtree ms
  JOIN Employees child ON child.ManagerID = ms.employee_id
)
SELECT m.EmployeeID, m.Name, m.ManagerID, d.DepartmentName, r.RoleName,
       proj.projects, tasks.tasks,
       (COUNT(ms.employee_id) - 1) AS total_subordinates
FROM manager_subtree ms
JOIN Employees m ON m.EmployeeID = ms.manager_id
JOIN Departments d ON d.DepartmentID = m.DepartmentID
JOIN Roles r ON r.RoleID = m.RoleID
LEFT JOIN LATERAL (
  SELECT STRING_AGG(p.ProjectName, ', ' ORDER BY p.ProjectName) AS projects
  FROM Projects p
  WHERE p.DepartmentID = m.DepartmentID
) proj ON TRUE
LEFT JOIN LATERAL (
  SELECT STRING_AGG(t.TaskName, ', ' ORDER BY t.TaskName) AS tasks
  FROM Tasks t
  WHERE t.AssignedTo = m.EmployeeID
) tasks ON TRUE
GROUP BY m.EmployeeID, m.Name, m.ManagerID, d.DepartmentName, r.RoleName, proj.projects, tasks.tasks
HAVING (COUNT(ms.employee_id) - 1) > 0
ORDER BY m.Name;
