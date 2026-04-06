-- Задание 4.2
-- Иван Иванов (EmployeeID = 1): все подчиненные + кол-во задач/прямых подчиненных

WITH RECURSIVE emp_tree AS (
  SELECT e.EmployeeID, e.Name, e.ManagerID, e.DepartmentID, e.RoleID
  FROM Employees e
  WHERE e.EmployeeID = 1
  UNION ALL
  SELECT e.EmployeeID, e.Name, e.ManagerID, e.DepartmentID, e.RoleID
  FROM Employees e
  JOIN emp_tree et ON e.ManagerID = et.EmployeeID
)
SELECT et.EmployeeID, et.Name, et.ManagerID, d.DepartmentName, r.RoleName,
       proj.projects, tasks.tasks,
       tasks.task_count AS total_tasks,
       (SELECT COUNT(*) FROM Employees direct WHERE direct.ManagerID = et.EmployeeID) AS total_direct_subordinates
FROM emp_tree et
JOIN Departments d ON d.DepartmentID = et.DepartmentID
JOIN Roles r ON r.RoleID = et.RoleID
LEFT JOIN LATERAL (
  SELECT STRING_AGG(p.ProjectName, ', ' ORDER BY p.ProjectName) AS projects
  FROM Projects p
  WHERE p.DepartmentID = et.DepartmentID
) proj ON TRUE
LEFT JOIN LATERAL (
  SELECT STRING_AGG(t.TaskName, ', ' ORDER BY t.TaskName) AS tasks,
         COUNT(*) AS task_count
  FROM Tasks t
  WHERE t.AssignedTo = et.EmployeeID
) tasks ON TRUE
ORDER BY et.Name;
