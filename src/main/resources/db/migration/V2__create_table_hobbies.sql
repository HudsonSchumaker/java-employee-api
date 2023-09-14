CREATE TABLE IF NOT EXISTS hobbies (
  id uuid PRIMARY KEY,
  employee_id uuid NOT NULL,
  name VARCHAR(64) NOT NULL,

  FOREIGN KEY (employee_id) REFERENCES employees(id)
);