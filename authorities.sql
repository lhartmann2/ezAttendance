/*Run this script to insert an admin user with the password 'changeme'*/
USE attendance;

INSERT INTO `users` (id, username, password, enabled, failed_attempt, account_non_locked) VALUES
(1, 'admin', '$2a$12$YlBhHk2HD2IDsHO0gw6JBuxJRePCB5emXgkbR0aSrR/RPXwVh8ucG', 1, 0, 1);

INSERT INTO `users_roles` VALUES
	(1, 1),
    (1, 2),
    (1, 3);