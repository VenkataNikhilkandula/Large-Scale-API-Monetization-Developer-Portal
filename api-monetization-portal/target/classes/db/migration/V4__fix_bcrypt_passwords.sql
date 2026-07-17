-- V4: Correct the BCrypt password hash for admin and developer users to match 'password123'
UPDATE users SET password = '$2a$10$jMYCiH.Ok09fpUt4NxsdZOOY9BI.s8bUOoex46DwpM8GT9FQx3PQu'
WHERE username IN ('admin', 'developer');
