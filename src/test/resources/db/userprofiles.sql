INSERT INTO `users` (`id`, `username`, `lastpoll`) VALUES (1, 'testuser1', NULL);
INSERT INTO `users` (`id`, `username`, `lastpoll`) VALUES (2, 'testuser2', NULL);

INSERT INTO `profiles` (`id`, `name`) VALUES (1, 'testprofile1');
INSERT INTO `profiles` (`id`, `name`) VALUES (2, 'testprofile2');

INSERT INTO `profile_users` (`id`, `userID`, `profileID`) VALUES (1, 1, 1);
INSERT INTO `profile_users` (`id`, `userID`, `profileID`) VALUES (2, 1, 2);
INSERT INTO `profile_users` (`id`, `userID`, `profileID`) VALUES (3, 2, 1);