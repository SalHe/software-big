<template>
  <div id="echartsRoot">
    <div id="echartsMain" style="width: 600px; height: 400px"></div>
  </div>
</template>

<script>
export default {
  name: "Echarts",
  data() {
    return {};
  },
  props:{
    proparray:Array
  },
  methods: {

    myEcharts() {
      var myChart = this.$echarts.init(document.getElementById("echartsMain"));
      //配置图表
      var option = {
        title: {
          text: "老师评价统计",
          left: "center",
        },
        tooltip: {
          trigger: "item",
        },
        legend: {
          orient: "vertical",
          left: "left",
        },
        series: [
          {
            name: "Comments",
            type: "pie",
            radius: "50%",
            data: this.proparray,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.5)",
              },
            },
          },
        ],
      };
      myChart.setOption(option);
    },
    dispose(){
       var myChart = this.$echarts.getInstanceByDom(document.getElementById("echartsMain"));
       myChart.dispose();
    }
  },
  mounted() {
    console.log("echarts called");
    console.log(this.proparray);
  },
};
</script>

<style>
</style>