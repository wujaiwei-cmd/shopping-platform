INSERT INTO user (username, password, email, phone, enabled, is_admin, created_at) 
SELECT * FROM (SELECT 
    'admin' AS username, 
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq' AS password, 
    'admin@example.com' AS email, 
    '13800138000' AS phone, 
    true AS enabled, 
    true AS is_admin, 
    NOW() AS created_at
) AS temp 
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');
