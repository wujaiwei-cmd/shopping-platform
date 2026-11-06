-- ============================================================
-- 网络购物平台 种子数据
-- 默认管理员账号: admin / admin123 （BCrypt 加密）
-- ============================================================

USE `shopping`;

-- 管理员账号
INSERT INTO `user` (username, password, email, phone, enabled, is_admin, created_at)
SELECT * FROM (
  SELECT
    'admin' AS username,
    '$2b$10$RB7Oqi2iZmYKUOj6oadChO3WpX6716HfpGSN4qZwrFL7IUt1dFVii' AS password,
    'admin@example.com' AS email,
    '13800138000' AS phone,
    1 AS enabled,
    1 AS is_admin,
    NOW() AS created_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'admin');

-- 常用商品分类
INSERT INTO `category` (name, description, sort_order, enabled) VALUES
  ('手机数码', '手机、平板、数码配件',     1, 1),
  ('电脑办公', '笔记本、台式机、外设',     2, 1),
  ('家用电器', '电视、空调、洗衣机',       3, 1),
  ('服饰鞋包', '男装、女装、鞋靴、箱包',   4, 1),
  ('美妆个护', '化妆品、护肤、个人护理',   5, 1),
  ('食品生鲜', '零食、饮料、生鲜',         6, 1),
  ('图书音像', '图书、有声、电子书',       7, 1),
  ('运动户外', '运动器材、户外装备',       8, 1)
ON DUPLICATE KEY UPDATE description = VALUES(description), sort_order = VALUES(sort_order);
