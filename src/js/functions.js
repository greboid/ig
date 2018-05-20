function getSizes() {
  var w = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
  var h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
  if (window.matchMedia("(min-resolution: 100dpi)").matches) {
    var d = 200;
  } else {
    var d = 100;
  }

  var urlParams = new URLSearchParams(window.location.search);
  urlParams.set('w', w);
  urlParams.set('h', h);
  urlParams.set('d', d);
  location = location.protocol + '//' + location.host + location.pathname + '?' + urlParams.toString();
  setTimeout(function() {
    window.location.href = location;
  }, 300);
}
