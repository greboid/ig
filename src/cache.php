<?php

require __DIR__ . '/../vendor/autoload.php';
require __DIR__ . '/../config.php';

$dir = realpath(dirname(__FILE__));

$cachetime = 30;
$instagram = new \InstagramScraper\Instagram();

echo "\r\n".'Checking files to cache.'."\r\n";
foreach ($usernames as $username) {
	echo "Caching: ".$username."\r\n";
        $filename = $dir.'/../cache/'.$username;
        $checktime = strtotime ('-'.$cachetime.' minutes');
        if (!file_exists($filename) || filemtime($filename) - $checktime <= $cachetime) {
		echo "Caching: ".$username."\r\n";
                $response = $instagram->getMedias($username, 10);
                file_put_contents($filename, serialize($response));
        }
}
foreach ($safeusernames as $username) {
        $filename = $dir.'/../safecache/'.$username;
        $checktime = strtotime ('-'.$cachetime.' minutes');
        if (!file_exists($filename) || filemtime($filename) - $checktime <= $cachetime) {
		echo "Caching: ".$username."\r\n";
                $response = $instagram->getMedias($username, 10);
                file_put_contents($filename, serialize($response));
        }
}
echo 'Finished caching.';

?>
