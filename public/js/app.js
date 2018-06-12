Vue.component('lightbox', {
  props: ['image'],
  data: function () {
    return {
      image: this.image,
    }
  },
  template: '<a class="item" :title="image.source" :href="image.url"><img class="itemimage" :src="image.thumb"></a>'
})

    var app = new Vue({
      el: '#app',
      data: {
        images: [],
	newimages: [],
      },
      methods: {
        checkAdd: function() {
          if (window.innerHeight + window.scrollY >= (document.body.offsetHeight)){
		this.fetchImages(this.images.length, 5)
	  }
        },
        scroll: function() {
          window.onscroll = ev => {
            if (window.innerHeight + window.scrollY >= (document.body.offsetHeight)){
              this.fetchImages(this.images.length, 5)
            }
          }
        },
        fetchImages: function(start, count) {
	  fetch('/lechery/feed.php?start='+start+'&count='+count)
          .then(response => response.json())
          .then(json => {
		for(var item in json) {
			this.images.push(json[item])
		}
          })
        },
      },
      created() {
          this.fetchImages(0, 5)
      },
      mounted() {
        this.scroll()
      },
      watch: {
        images: function () {
  	       this.checkAdd()
        }
      }
    })
