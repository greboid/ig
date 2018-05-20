$(".item").click(function(e) {
	e.preventDefault();
	$('.item').simpleLightbox();
});
window.addEventListener("resize", getSizes);
