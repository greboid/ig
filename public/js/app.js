Vue.component('lightbox', {
  props: ['image'],
  data: function () {
    return {
      instanceImage: this.image,
    }
  },
  template: '<a class="item" :title="image.source" :href="image.url"><img class="itemimage" :src="image.thumb"></a>'
})

var app = new Vue({
  el: '#app',
  data: {
    images: [],
    newimages: [],
    busy: false,
    showModal: false,
  },
  methods: {
    checkAdd: function() {
      if (window.innerHeight + window.scrollY >= (document.body.offsetHeight)) {
        this.fetchImages(this.images.length, 5)
      }
    },
    scroll: function() {
      window.onscroll = ev => {
       if (window.innerHeight + window.scrollY >= (document.body.offsetHeight)) {
          this.fetchImages(this.images.length, 5)
        }
      }
    },
    fetchImages: function(start, count) {
      if (this.busy) {
        return
      }
      this.busy = true
      fetch('/feed.php?start='+start+'&count='+count)
      .then(response => response.json())
      .then(json => {
        for(var item in json) {
         this.images.push(json[item])
       }
     })
      this.busy = false
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
Vue.config.devtools=true
