-- V2__RBAC_Triggers.sql

DELIMITER //

/**
 * 触发器：角色parent_id更新前校验循环继承
 */
CREATE TRIGGER trg_role_before_update
BEFORE UPDATE ON role
FOR EACH ROW
BEGIN
    DECLARE is_circular INT DEFAULT 0;
    DECLARE current_parent BIGINT;

    IF NEW.parent_id IS NOT NULL AND (OLD.parent_id IS NULL OR NEW.parent_id != OLD.parent_id) THEN
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;

        SET current_parent = NEW.parent_id;
        SET is_circular = 0;

        WHILE current_parent IS NOT NULL AND is_circular = 0 DO
            IF current_parent = NEW.id THEN
                SET is_circular = 1;
            END IF;

            SELECT parent_id INTO current_parent
            FROM role
            WHERE id = current_parent AND is_deleted = 0;
        END WHILE;

        IF is_circular = 1 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '角色继承链形成循环，更新被拒绝';
        END IF;
    END IF;
END//

/**
 * 触发器：角色插入前校验parent_id有效性
 */
CREATE TRIGGER trg_role_before_insert
BEFORE INSERT ON role
FOR EACH ROW
BEGIN
    DECLARE parent_exists INT DEFAULT 0;

    IF NEW.parent_id IS NOT NULL THEN
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;

        SELECT COUNT(*) INTO parent_exists
        FROM role
        WHERE id = NEW.parent_id AND is_deleted = 0;

        IF parent_exists = 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '父角色不存在或已被删除';
        END IF;
    END IF;
END//

DELIMITER ;