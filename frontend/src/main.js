import Vue from "vue";
import VueRouter from "vue-router";
import router from "./routes";
import "./plugins/vuetify";
import App from "./containers/App/App.vue";
import "bootstrap/dist/css/bootstrap-grid.min.css";
import * as VueGoogleMaps from "vue2-google-maps";
import config from "./config";

Vue.use(VueRouter);



Vue.use(VueGoogleMaps, {
  // Don't load with key if environment variable isn't set
  load: config.GOOGLE_MAPS_KEY ? {
    key: config.GOOGLE_MAPS_KEY,
    libraries: "places"
  } : {
    libraries: "places"
  }
});

Vue.config.productionTip = false;

new Vue({
  el: "#app",
  router,
  render: h => h(App),
}).$mount("#app");
