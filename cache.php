<?php

require __DIR__ . '/vendor/autoload.php';
require __DIR__ . '/config.php';
require __DIR__ . '/shared.php';

$dir = realpath(dirname(__FILE__));

$cachetime = 30;
$instagram = new \InstagramScraper\Instagram();

echo date("Y-m-d H:i:s").' Checking files to cache.'.PHP_EOL;
foreach ($usernames as $username) {
        $filename = $dir.'/cache/'.$username;
        $checktime = strtotime ('-'.$cachetime.' minutes');
        if (!file_exists($filename) || filemtime($filename) - $checktime <= $cachetime) {
		echo date("Y-m-d H:i:s")." Caching: ".$username.PHP_EOL;
                $response = $instagram->getMedias($username, 10);
                file_put_contents($filename, serialize($response));
        }
}
foreach ($safeusernames as $username) {
        $filename = $dir.'/../safecache/'.$username;
        $checktime = strtotime ('-'.$cachetime.' minutes');
        if (!file_exists($filename) || filemtime($filename) - $checktime <= $cachetime) {
		echo date("Y-m-d H:i:s")." Caching: ".$username.PHP_EOL;
                $response = $instagram->getMedias($username, 10);
                file_put_contents($filename, serialize($response));
        }
}
echo date("Y-m-d H:i:s").' Finished caching.'.PHP_EOL;

?>
