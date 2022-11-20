<template>
  <div id="examManageRoot">
    <div id="examManageHeader">
      <el-button
        size="small"
        type="success"
        id="goHomeBtn"
        @click="goToHome"
        style="margin-left: 10px; margin-top: 10px"
      >
        返回
      </el-button>
      <el-divider direction="vertical"></el-divider>
      <el-input
        placeholder="请输入课程名"
        prefix-icon="el-icon-search"
        v-model="seacherClassWord"
        style="width: 200px; margin-top: 10px; margin-left: 10px"
      >
      </el-input>
      <el-button
        size="small"
        type="primary"
        id="searchPaperBtn"
        @click="searchClass"
        style="margin-left: 10px; margin-top: 10px"
      >
        搜索
      </el-button>
    </div>
    <el-divider content-position="left" id="examManageClassDiv">
      班级列表
    </el-divider>
    <div id="examManageClassInfoBox">
      <el-table
        :data="classes"
        style="width: 100%; border-radius: 25px"
        border
        stripe
        height="250"
      >
        <el-table-column prop="classId" label="课程号"> </el-table-column>
        <el-table-column prop="className" label="课程名"> </el-table-column>
        <el-table-column prop="owner" label="教师名称" v-if="!isTeacher">
        </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
            <el-button type="success" @click="goToExamPublish(scope.$index)">
              发布评教
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-divider content-position="left" id="examManageExamDiv">
      评教列表
    </el-divider>
    <div id="examManageInfoBox">
      <el-table
        :data="exams"
        style="width: 100%; border-radius: 25px"
        border
        stripe
        height="230"
      >
        <el-table-column prop="className" label="课程名" width=".25">
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width=".25">
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width=".25">
        </el-table-column>
        <el-table-column prop="problemSetName" label="评教名" width=".25">
        </el-table-column>
        <el-table-column prop="time" label="" width=".25">
          <template slot-scope="scope">
            <el-button type="success" @click="goToReview(scope.$index)">
              查看评教结果
            </el-button>
            <!-- <el-button
              type="success"
              v-if="isTeacher"
              @click="computeStuScores(scope.$index)"
            >
              成绩统计
            </el-button> -->
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog title="成绩统计" :visible.sync="dialogFormScore">
      <div id="scorePie" style="height: 600px; width: 400px"></div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="scoreCancel">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from "echarts";
import {
  GetCourseCreated,
  GetExamJoined,
  /*PostAnsList,*/
} from "../request/api.js";
export default {
  name: "ExamManage",
  data() {
    return {
      seacherClassWord: "",
      classes: [],
      exams: [],
      dialogFormScore: false,
      myChart: {},
    };
  },
  methods: {
    goToHome() {
      this.$router.push({ path: "/" });
    },
    goToExamPublish(index) {
      this.$router.push({
        name: "ExamPublish",
        params: {
          classId: this.classes[index].classId,
          className: this.classes[index].className,
        },
      });
    },
    goToReview(index) {
      this.$router.push({
        name: "Review",
        params: {
          problemSetId: this.exams[index].problemSetId,
          classId: this.exams[index].classId,
        },
      });
    },
    computeStuScores(index) {
      console.log(0);
      var myChart = echarts.init(document.getElementById("scorePie"), null, {
        width: 600,
        height: 400,
      });
      myChart.setOption({
        title: {
          text:
            this.exams[index].className +
            "-" +
            this.exams[index].problemSetName,
        },
        tooltip: {
          trigger: "item",
        },
        series: [
          {
            name: "Access From",
            type: "pie",
            radius: "50%",
            data: [
              { value: 1048, name: "Search Engine" },
              { value: 735, name: "Direct" },
              { value: 580, name: "Email" },
              { value: 484, name: "Union Ads" },
              { value: 300, name: "Video Ads" },
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.5)",
              },
            },
          },
        ],
      });
      console.log(1);
      this.dialogFormScore = true;
      console.log(2);
    },
    scoreCancel() {
      this.dialogFormScore = false;
    },
  },
  mounted() {
    GetExamJoined({}).then((res) => {
      this.exams = res.data.data;
    });
    GetCourseCreated().then((res) => {
      this.classes = res.data.data;
    });
  },
  computed: {
    isTeacher() {
      return sessionStorage.getItem("role") == "教师";
    },
  },
};
</script>

<style>
#examManageRoot {
  position: relative;
      height: 668px;
}
#examManageHeader {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 15px;
  height: 55px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 15px;
}
#examManageClassDiv {
  top: 95px;
  margin-left: 5%;
  width: 95%;
}
#examManageClassInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 115px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
    border-radius: 25px;
}
#examManageExamDiv {
  top: 365px;
  margin-left: 5%;
  width: 95%;
}
#examManageInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 415px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  margin-bottom: 15px;
    border-radius: 25px;
}
</style>