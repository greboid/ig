<?php

require __DIR__ . '/../vendor/autoload.php';
//require __DIR__ . '/../config.php';
//require __DIR__ . '/../shared.php';

$filename = __DIR__ . '/../database.sqlite';

$offset = filter_input(INPUT_GET, 'start', FILTER_VALIDATE_INT, array("options" => array("default" => 0)));
$count = filter_input(INPUT_GET, 'count', FILTER_VALIDATE_INT, array("options" => array("default" => 5)));

try {
	        $images = array();

        try {
                $file_db = new PDO('sqlite:'.$filename);
                $stmt = $file_db->prepare('SELECT shortcode, medias.username as source, thumbnailURL as thumb, imageURL as url, caption as caption, timestamp
                                                FROM medias
                                                LEFT JOIN users on users.username=medias.username
                                                ORDER BY timestamp DESC
						LIMIT '.$count.' OFFSET '.$offset.'
                                        ');
                $stmt->execute();
                $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
        } catch(PDOException $e) {
                echo date("Y-m-d H:i:s").' Error: '.$e->getMessage().PHP_EOL;
        }
        $images = $results;
	$images = array_slice($images, 0, 250);
	header('Content-Type: application/json');
	echo json_encode($images);
} catch (Exception $e) {
	die ('ERROR: ' . $e->getMessage());
}

function epoch_cmp($a, $b) {
        if ($a['datetime'] < $b['datetime']) {
                return 1;
        } else if ($a['datetime'] > $b['datetime']) {
                return -1;
        } else {
                return 0;
        }
}
?>
