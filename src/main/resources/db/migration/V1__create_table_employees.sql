CREATE TABLE IF NOT EXISTS employees (
   id uuid PRIMARY KEY,
   email VARCHAR(64) NOT NULL UNIQUE,
   full_name VARCHAR(64) NOT NULL,
   date_of_birth DATE NOT NULL,
   created_on TIMESTAMP NOT NULL,
   updated_on TIMESTAMP NOT NULL
);