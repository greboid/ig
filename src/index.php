<?php

require __DIR__ . '/../vendor/autoload.php';
require __DIR__ . '/../config.php';
require __DIR__ . '/../shared.php';

$filename = __DIR__ . '/../cache/database.sqlite3';

try {
	$loader = new Twig_Loader_Filesystem('../templates');
	$twig = new Twig_Environment($loader);
	$twig = new Twig_Environment($loader, array(
		'debug' => true,
	));
	$twig->addExtension(new Twig_Extension_Debug());

	if (!filter_has_var(INPUT_GET, 'w') || !filter_has_var(INPUT_GET, 'h') || !filter_has_var(INPUT_GET, 'd')) {
		$template = $twig->loadTemplate('redirect.tpl');
		echo $template->render(array());
		die();
	}
	$width = filter_input(INPUT_GET, 'w', FILTER_VALIDATE_INT);
	$height = filter_input(INPUT_GET, 'h', FILTER_VALIDATE_INT);
	$dpi = filter_input(INPUT_GET, 'd', FILTER_VALIDATE_INT);
	$area = $width * $height;
	$imagearea = $dpi * $dpi;
	if ($imagearea == 0) {
		$imagearea = 1;
	}
	if ($area == 0) {
		$area = 0;
	}
	$number = $area / $imagearea;
	$number = round($number + 1);
	$ip = filter_input(INPUT_SERVER, 'REMOTE_ADDR', FILTER_VALIDATE_IP);
	if (isset($_GET['safemode']) && $_GET['safemode'] == 'off') {
		$safemode = 0;
	} else if (isset($_GET['safemode'])) {
		$safemode = 1;
	} else if ($ip == '51.148.129.108') {
		$safemode = 0;
	} else {
		$safemode = 1;
	}
	$images = array();

	try {
		$file_db = new PDO('sqlite:'.$filename);
		$stmt = $file_db->prepare('SELECT shortcode, medias.username as source, thumbnailURL as thumb, imageURL as url, caption as caption, timestamp
						FROM medias
						LEFT JOIN users on users.username=medias.username
						WHERE users.safemode=:safemode
						ORDER BY timestamp DESC
					');
		$stmt->bindValue(':safemode', $safemode);
		$stmt->execute();
		$results = $stmt->fetchAll(PDO::FETCH_ASSOC);
	} catch(PDOException $e) {
		echo date("Y-m-d H:i:s").' Error: '.$e->getMessage().PHP_EOL;
	}
	$images = $results;

	$template = $twig->loadTemplate('index.tpl');
	echo $template->render(array(
		'images' => $images,
	));
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
