<!DOCTYPE html>
<html>
	<head>
		<title>Instagram</title>
		<link rel="stylesheet" href="/lechery/css/main.css" />
		<link rel="stylesheet" href="/lechery/css/simplelightbox.css" />
	</head>
	<body>
		<div class="container">
			{% for image in images %}
				<a class="item" title="{{image.source}}<br>{{image.caption}}" href="{{image.url}}"><img class="itemimage" src="{{image.thumb}}" title="{{image.source}}"></a>
			{% else %}
				<p>Nothing to see.</p>
			{% endfor %}
</ul>
		</div>
		<script src="/lechery/js/jquery.min.js" type="text/javascript" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
		<script src="/lechery/js/simplelightbox.min.js" type="text/javascript" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
                <script src="/lechery/js/functions.js" type="text/javascript" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
		<script src="/lechery/js/script.js" type="text/javascript" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
	</body>
</html>
