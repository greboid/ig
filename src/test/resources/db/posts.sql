INSERT INTO `users` (`id`, `username`, `lastpoll`) VALUES (1, 'testuser1', NULL);
INSERT INTO `users` (`id`, `username`, `lastpoll`) VALUES (2, 'testuser2', NULL);

INSERT INTO `profiles` (`id`, `name`) VALUES (1, 'testprofile1');
INSERT INTO `profiles` (`id`, `name`) VALUES (2, 'testprofile2');

INSERT INTO `profile_users` (`id`, `userID`, `profileID`) VALUES (1, 1, 1);
INSERT INTO `profile_users` (`id`, `userID`, `profileID`) VALUES (2, 2, 2);

INSERT INTO `igposts` (`shortcode`, `ord`, `userID`, `thumbnailURL`, `imageURL`, `caption`, `timestamp`) VALUES
	('RSLBmnFzrba', 0, 1, 'thumbs\\RSLBmnFzrba.jpg', 'https://www.example.com/RSLBmnFzrba', '... 👍🏼(😂)', 1545834162),
	('Uv6J1JGURH2', 0, 1, 'thumbs\\Uv6J1JGURH2.jpg', 'https://www.example.com/Uv6J1JGURH2', 'foo', 1546079082),
	('IIpRRMXowSZ', 0, 1, 'thumbs\\IIpRRMXowSZ.jpg', 'https://www.example.com/IIpRRMXowSZ', 'bar', 1544999111),
	('IIpRRMXowSZ', 1, 1, 'thumbs\\pje5bpJSrjx.jpg', 'https://www.example.com/pje5bpJSrjx', 'bar', 1544999111),
	('Z5Z8TJG7X9p', 0, 1, 'thumbs\\Z5Z8TJG7X9p.jpg', 'https://www.example.com/Z5Z8TJG7X9p', 'baz', 1545159719),
	('YsE1tF0WXcV', 0, 1, 'thumbs\\YsE1tF0WXcV.jpg', 'https://www.example.com/YsE1tF0WXcV', '🌚 😁 😉', 1545334021),
	('FCpZf-gi3la', 0, 2, 'thumbs\\FCpZf-gi3la.jpg', 'https://www.example.com/FCpZf-gi3la', 'foobarbaz', 1544558401),
	('5iBGkkHd1OR', 0, 2, 'thumbs\\5iBGkkHd1OR.jpg', 'https://www.example.com/5iBGkkHd1OR', 'eeep', 1545570021),
	('5iBGkkHd1OR', 2, 2, 'thumbs\\CB9dMeqVzk2.jpg', 'https://www.example.com/CB9dMeqVzk2', 'rar', 1544737152),
	('5iBGkkHd1OR', 3, 2, 'thumbs\\f1lulGTQdmR.jpg', 'https://www.example.com/f1lulGTQdmR', 'Caption', 1544737152)