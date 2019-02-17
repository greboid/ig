INSERT INTO `accounts` (`id`, `username`, `password`, `isadmin` ) VALUES (1, 'testuser1', 'password1', true);

INSERT INTO `SourceTypes` (`id`, `name`) VALUES (1, 'TestType');

INSERT INTO `Sources` (`id`, `name`, `lastpoll`, `SourceType_ID`) VALUES (1, 'testsource1', NULL, 1);
INSERT INTO `Sources` (`id`, `name`, `lastpoll`, `SourceType_ID`) VALUES (2, 'testsource2', NULL, 1);

INSERT INTO `categories` (`id`, `name`, `account_id`) VALUES (1, 'testcategories1', 1);
INSERT INTO `categories` (`id`, `name`, `account_id`) VALUES (2, 'testcategories2', 1);

INSERT INTO `CategoryMap` (`id`, `categories_ID`, `source_ID`) VALUES (1, 1, 1);
INSERT INTO `CategoryMap` (`id`, `categories_ID`, `source_ID`) VALUES (2, 1, 2);
INSERT INTO `CategoryMap` (`id`, `categories_ID`, `source_ID`) VALUES (3, 2, 1);