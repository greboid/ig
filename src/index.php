<?php

require __DIR__ . '/../vendor/autoload.php';
require __DIR__ . '/../config.php';
require __DIR__ . '/../shared.php';

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
    $dir = '../cache/';
    $usernames = array_diff(scandir($dir), array('..', '.'));
  } else if (isset($_GET['safemode'])) {
    $dir = $dir = '../safecache/';
    $usernames = array_diff(scandir($dir), array('..', '.'));
  } else if ($ip == '51.148.129.108') {
    $dir = '../cache/';
    $usernames = array_diff(scandir($dir), array('..', '.'));
  } else {
    $dir = '../safecache/';
    $usernames = array_diff(scandir($dir), array('..', '.'));
  }
  $images = array();
  foreach ($usernames as $id=>$username) {
    $filename = $dir.$username;
    $filecontents = file_get_contents($filename);
    if (strlen($filecontents) == 0) {
      continue;
    }
    $response = unserialize($filecontents);
    if ($response === FALSE) {
      continue;
    }
    $medias = array();
    foreach ($response as $media) {
      $media = array('datetime'=>$media->getCreatedTime(), 'thumb'=>$media->getImageThumbnailUrl(), 'url'=>$media->getImageHighResolutionUrl(), 'source'=>$username, 'caption'=>$media->getCaption());
      $medias[] = $media;
    }
    $images[$username] = $medias;
  }
  $images = array_flatten(rotate90($images));
  uasort($images, 'epoch_cmp');


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


function array_flatten($array) {
  $result = array();
  foreach ($array as $key=>$value) {
    foreach ($value as $subkey=>$subvalue) {
      $result[] = $subvalue;
    }
  }
  return $result;
}

function rotate90($array_one) {
  $array_two = [];
  foreach ($array_one as $key => $item) {
    foreach ($item as $subkey => $subitem) {
      $array_two[$subkey][$key] = $subitem;
    }
  }
  return $array_two;
}
?>
