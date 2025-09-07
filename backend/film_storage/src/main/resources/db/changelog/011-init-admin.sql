--liquibase formatted sql

--changeset danilgordienko:011-init-admin
INSERT INTO users (username, email, password, rating_visibility)
SELECT 'danilgordienko',
       'danilgordienko212@gmail.com',
       '$2a$10$cDUs7wAGS8nvvlCG5rqwRutmuHiRxwcTbS4HXLgz5LLzl7Jw5Bgbe',
       'ALL'
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'danilgordienko'
);

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.username = 'danilgordienko'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role = 'ADMIN'
);
