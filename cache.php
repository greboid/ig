<?php

require __DIR__ . '/vendor/autoload.php';
require __DIR__ . '/config.php';
require __DIR__ . '/shared.php';

$cachetime = 30;
$instagram = new \InstagramScraper\Instagram();

$filename = __DIR__ . '/cache/database.sqlite3';
try {
	$file_db = new PDO('sqlite:'.$filename);
	$file_db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	$file_db->exec('CREATE TABLE IF NOT EXISTS users (
				id INTEGER PRIMARY KEY,
				username TEXT UNIQUE,
				safemode INTEGER,
				lastpoll INTEGER
			)
	');
	$file_db->exec('CREATE TABLE IF NOT EXISTS medias (
				shortcode TEXT PRIMARY KEY,
				username TEXT,
				thumbnailURL TEXT,
				imageURL TEXT,
				caption TEXT,
				timestamp INTEGER
			)
	');
	$stmt = $file_db->prepare('INSERT OR IGNORE INTO users (username, safemode) VALUES (:username, :safemode)');
	foreach ($usernames as $username) {
		$stmt->bindValue(':username', $username);
		$stmt->bindValue(':safemode', 0);
		$stmt->execute();
	}
	foreach ($safeusernames as $username) {
		$stmt->bindValue(':username', $username);
		$stmt->bindValue(':safemode', 1);
		$stmt->execute();
	}
	echo date("Y-m-d H:i:s").' Starting cache run.'.PHP_EOL;
	$stmt = $file_db->prepare('SELECT username,lastpoll FROM users');
	$stmt->execute();
	$users = $stmt->fetchAll(PDO::FETCH_ASSOC);
	$lastpollstmt = $file_db->prepare('UPDATE users set lastpoll=:lastpoll');
	$mediastmt = $file_db->prepare('INSERT OR IGNORE INTO medias (shortcode, username, thumbnailURL, imageURL, caption, timestamp) VALUES (:shortcode, :username, :thumbnailURL, :imageURL, :caption, :timestamp)');
	foreach ($users as $id => $user) {
		echo date("Y-m-d H:i:s").' Checking user: '.$user['username'].PHP_EOL;
		$checktime = strtotime ('-'.$cachetime.' minutes');
		if ($user['lastpoll'] - $checktime <= $cachetime) {
			echo date("Y-m-d H:i:s").' Caching user: '.$user['username'].PHP_EOL;
			$medias = $instagram->getMedias($user['username'], 10);
			$lastpollstmt->bindValue(':lastpoll', time());
			foreach ($medias as $media) {
				$mediastmt->bindValue(':username', $user['username']);
				$mediastmt->bindValue(':shortcode', $media->getShortCode());
				$mediastmt->bindValue(':thumbnailURL', $media->getImageThumbnailUrl());
				$mediastmt->bindValue(':imageURL', $media->getImageHighResolutionUrl());
				$mediastmt->bindValue(':caption', $media->getCaption());
				$mediastmt->bindValue(':timestamp', $media->getCreatedTime());
				$mediastmt->execute();
			}
			$lastpollstmt->execute();
		}
	}
} catch(PDOException $e) {
        echo date("Y-m-d H:i:s").' Error: '.$e->getMessage().PHP_EOL;
}
echo date("Y-m-d H:i:s").' Finished cache run.'.PHP_EOL;
?>
