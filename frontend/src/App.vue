<template>
  <div id="app" :style="backgroundDiv" >
    <Banner />
    <router-view></router-view>
  </div>
</template>

<script>
import Banner from "./components/Banner.vue";

export default {
  name: "App",
  components: {
    Banner,
  },
  mounted() {
  },
  data() {
    return {
      backgroundDiv: {
        backgroundImage: 'url(' + require('./assets/backgroundImg.jpg') + ')',
        backgroundRepeat: 'no-repeat',
      }
    }
  },
};
</script>

<style>
body{
  margin: 0;
}
#app{
  width: 100%;
}
.el-divider__text.is-left {
  color: rgb(11, 11, 11);
  background: rgb(194, 194, 194);
  font-weight: bold;
  border-radius: 5px;
  text-shadow: 0 0 5px white;
}
.el-divider__text.is-center {
  color: rgb(11, 11, 11);
  background: rgb(137, 137, 137);
  font-weight: bold;
  border-radius: 5px;
  text-shadow: 0 0 5px white;
}
</style>
