-- ============================================================
-- 无定珍 种子数据
-- 包含：管理员账号、测试用户、8 类系统食材、示例食谱、标签
-- ============================================================

USE `palatophil`;

-- ============================================================
-- 1. 管理员账号（密码 admin123，已 BCrypt 加密）
-- 2. 测试用户（密码 user123）
-- ============================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role`, `status`, `created_at`)
VALUES
  (1, 'admin',     '$2a$10$fMKKpJ1pn7ks.MIiGUP5MuZYT7OA6LL7C9PctRhpcrqpP/nm129NK', '系统管理员', 'ADMIN', 1, NOW()),
  (2, 'testuser',  '$2a$10$v8TN5e6py5QyG.f3tEQmrOY.iRjR8n.HqiMYqaHXGlkLIJXBkrvZ6', '小明',       'USER',  1, NOW()),
  (3, NULL, NULL, '小红', 'USER', 1, NOW());

-- ============================================================
-- 3. 系统食材库（8 类，每类 5+ 种，共 48 条）
-- ============================================================
INSERT INTO `standard_ingredient` (`name`, `aliases`, `category`, `is_system`, `audit_status`) VALUES
  -- 蔬菜 VEGETABLE
  ('西红柿', JSON_ARRAY('番茄', '洋柿子'), 'VEGETABLE', 1, 1),
  ('土豆', JSON_ARRAY('马铃薯', '洋芋'), 'VEGETABLE', 1, 1),
  ('胡萝卜', JSON_ARRAY('萝卜'), 'VEGETABLE', 1, 1),
  ('青椒', JSON_ARRAY('柿子椒', '甜椒'), 'VEGETABLE', 1, 1),
  ('白菜', JSON_ARRAY('大白菜'), 'VEGETABLE', 1, 1),
  ('生菜', JSON_ARRAY('叶生菜'), 'VEGETABLE', 1, 1),
  ('黄瓜', JSON_ARRAY('青瓜'), 'VEGETABLE', 1, 1),
  ('茄子', JSON_ARRAY('紫茄'), 'VEGETABLE', 1, 1),

  -- 肉类 MEAT
  ('猪肉', JSON_ARRAY('五花肉', '瘦肉'), 'MEAT', 1, 1),
  ('牛肉', JSON_ARRAY('牛里脊'), 'MEAT', 1, 1),
  ('鸡肉', JSON_ARRAY('鸡胸肉'), 'MEAT', 1, 1),
  ('排骨', JSON_ARRAY('猪排骨'), 'MEAT', 1, 1),
  ('羊肉', JSON_ARRAY('羊腿肉'), 'MEAT', 1, 1),

  -- 水产 AQUATIC
  ('草鱼', JSON_ARRAY('鲩鱼'), 'AQUATIC', 1, 1),
  ('鲈鱼', JSON_ARRAY('鲈花'), 'AQUATIC', 1, 1),
  ('带鱼', JSON_ARRAY('刀鱼'), 'AQUATIC', 1, 1),
  ('虾', JSON_ARRAY('基围虾'), 'AQUATIC', 1, 1),
  ('螃蟹', JSON_ARRAY('大闸蟹'), 'AQUATIC', 1, 1),

  -- 主食 GRAIN
  ('大米', JSON_ARRAY('稻米', '白米'), 'GRAIN', 1, 1),
  ('面粉', JSON_ARRAY('中筋粉'), 'GRAIN', 1, 1),
  ('面条', JSON_ARRAY('挂面'), 'GRAIN', 1, 1),
  ('小米', JSON_ARRAY('粟米'), 'GRAIN', 1, 1),
  ('玉米', JSON_ARRAY('苞谷'), 'GRAIN', 1, 1),

  -- 调味 SEASONING
  ('盐', JSON_ARRAY(), 'SEASONING', 1, 1),
  ('酱油', JSON_ARRAY('生抽'), 'SEASONING', 1, 1),
  ('醋', JSON_ARRAY('米醋'), 'SEASONING', 1, 1),
  ('糖', JSON_ARRAY('白砂糖'), 'SEASONING', 1, 1),
  ('料酒', JSON_ARRAY(), 'SEASONING', 1, 1),
  ('花椒', JSON_ARRAY(), 'SEASONING', 1, 1),
  ('八角', JSON_ARRAY('大料'), 'SEASONING', 1, 1),

  -- 乳制品 DAIRY
  ('牛奶', JSON_ARRAY('纯牛奶'), 'DAIRY', 1, 1),
  ('黄油', JSON_ARRAY('白脱'), 'DAIRY', 1, 1),
  ('酸奶', JSON_ARRAY(), 'DAIRY', 1, 1),
  ('奶酪', JSON_ARRAY('芝士'), 'DAIRY', 1, 1),

  -- 水果 FRUIT
  ('苹果', JSON_ARRAY(), 'FRUIT', 1, 1),
  ('香蕉', JSON_ARRAY(), 'FRUIT', 1, 1),
  ('橙子', JSON_ARRAY('柳丁'), 'FRUIT', 1, 1),
  ('葡萄', JSON_ARRAY(), 'FRUIT', 1, 1),
  ('西瓜', JSON_ARRAY(), 'FRUIT', 1, 1),

  -- 其他 OTHER
  ('鸡蛋', JSON_ARRAY('土鸡蛋'), 'OTHER', 1, 1),
  ('豆腐', JSON_ARRAY('嫩豆腐'), 'OTHER', 1, 1),
  ('木耳', JSON_ARRAY('黑木耳'), 'OTHER', 1, 1),
  ('香菇', JSON_ARRAY('冬菇'), 'OTHER', 1, 1),
  ('葱', JSON_ARRAY('大葱'), 'OTHER', 1, 1),
  ('姜', JSON_ARRAY('生姜'), 'OTHER', 1, 1),
  ('蒜', JSON_ARRAY('大蒜'), 'OTHER', 1, 1);

-- ============================================================
-- 4. 系统标签
-- ============================================================
INSERT INTO `recipe_tag` (`name`, `is_system`) VALUES
  ('快手菜', 1),
  ('家常菜', 1),
  ('低脂', 1),
  ('高蛋白', 1),
  ('素食', 1),
  ('辣', 1),
  ('清淡', 1),
  ('汤羹', 1),
  ('主菜', 1),
  ('一锅出', 1);

-- ============================================================
-- 5. 示例食谱（3 个）
-- ============================================================

-- 食谱 1：西红柿炒蛋
INSERT INTO `recipe` (`id`, `name`, `description`, `owner_id`, `visibility`, `audit_status`, `version`, `servings`)
VALUES (1, '西红柿炒蛋', '经典家常菜，酸甜可口', 2, 'PUBLIC', 1, 1, 2);

INSERT INTO `recipe_block` (`id`, `recipe_id`, `name`, `block_type`, `sort_order`)
VALUES (1, 1, '主料', 'SINGLE', 1);

INSERT INTO `recipe_ingredient` (`recipe_id`, `block_id`, `ingredient_id`, `amount_g`, `is_anchor`) VALUES
  (1, 1, 1, 250.0, 1),  -- 西红柿 250g
  (1, 1, 42, 200.0, 0); -- 鸡蛋 200g (约4个)

-- 食谱 2：土豆炖排骨
INSERT INTO `recipe` (`id`, `name`, `description`, `owner_id`, `visibility`, `audit_status`, `version`, `servings`)
VALUES (2, '土豆炖排骨', '浓香软糯的家常炖菜', 2, 'PUBLIC', 1, 1, 3);

INSERT INTO `recipe_block` (`id`, `recipe_id`, `name`, `block_type`, `sort_order`)
VALUES (2, 2, '主料', 'SINGLE', 1);

INSERT INTO `recipe_ingredient` (`recipe_id`, `block_id`, `ingredient_id`, `amount_g`, `is_anchor`) VALUES
  (2, 2, 11, 600.0, 1),  -- 排骨 600g
  (2, 2, 2,  450.0, 0),  -- 土豆 450g
  (2, 2, 3,  150.0, 0),  -- 胡萝卜 150g
  (2, 2, 25, 10.0, 0);   -- 盐 10g

-- 食谱 3：清蒸鲈鱼
INSERT INTO `recipe` (`id`, `name`, `description`, `owner_id`, `visibility`, `audit_status`, `version`, `servings`)
VALUES (3, '清蒸鲈鱼', '清淡鲜美，保留食材原味', 2, 'PUBLIC', 1, 1, 2);

INSERT INTO `recipe_block` (`id`, `recipe_id`, `name`, `block_type`, `sort_order`)
VALUES (3, 3, '主料', 'SINGLE', 1);

INSERT INTO `recipe_ingredient` (`recipe_id`, `block_id`, `ingredient_id`, `amount_g`, `is_anchor`) VALUES
  (3, 3, 16, 500.0, 1),  -- 鲈鱼 500g
  (3, 3, 47, 20.0, 0),   -- 姜 20g
  (3, 3, 46, 20.0, 0);   -- 葱 20g

-- 标签关联
INSERT INTO `recipe_tag_rel` (`recipe_id`, `tag_id`) VALUES
  (1, 1), (1, 2),            -- 西红柿炒蛋：快手菜、家常菜
  (2, 2), (2, 9),            -- 土豆炖排骨：家常菜、主菜
  (3, 7), (3, 8);            -- 清蒸鲈鱼：清淡、汤羹
