<template>
  <div id="welcomeRoot">
    <div id="welcomeWord">
      This system is done for students to comment on teachers.<br />
      <br /><br /><br />
      <br />
    </div>
  </div>
</template>

<script>
export default {
  name: "Welcome",
  data() {
    return {};
  },
  mounted() {},
  methods: {},
};
</script>

<style>
#welcomeRoot {
  margin: 0;
  height: 591px;
}
#welcomeWord {
  margin: 0 auto;
  font-size: 40px;
  color: white;
  user-select: none;
  text-align: center;
  margin-top: 100px;
  text-shadow: 1px 1px 3px black;
}
</style>