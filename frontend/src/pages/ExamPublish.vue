<template>
  <div id="examPublishRoot">
    <el-divider>发布考试</el-divider>
    <el-form id="publishExamForm">
      <div id="publishExamBox">
        <span class="el-form-item__label">当前班级:{{ className }}</span>
        <br /><br />
        <span class="el-form-item__label">对应课程号:{{ classId }}</span>
        <br /><br />
        <el-form-item label="选择试卷:">
          <el-select
            v-model="problemSetId"
            filterable
            placeholder="请选择试卷号"
          >
            <el-option
              v-for="item in problemSetData"
              :key="item.problemSetId"
              :label="item.problemSetId"
              :value="item.problemSetId"
            >
              <span style="float: left">
                {{ item.problemSetId }}
              </span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ item.problemSetName }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="time"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-button
          id="publishExamBtn"
          @click="publishExamBtn"
          type="primary"
          style="float: right; margin: 10px"
        >
          确定发布
        </el-button>
        <el-button
          id="goToQuestionBankBtn"
          @click="goToExamManage"
          type="success"
          style="float: right; margin: 10px"
        >
          返回
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { GetExamCreated, PostExam } from "../request/api";
export default {
  name: "ExamPublish",
  data() {
    return {
      className: "",
      classId: -1,
      time: "",
      problemSetId: -1,

      problemSetData: [],
    };
  },
  methods: {
    goToExamManage() {
      this.$router.push({ name: "ExamManage" });
    },
    publishExamBtn() {
      if (
        this.startTime == "" ||
        this.endTime == "" ||
        this.problemSetId == -1 ||
        this.classId == -1
      ) {
        this.$message("信息不全");
      } else {
        PostExam({
          problemSetId: this.problemSetId,
          classId: this.classId,
          startTime: this.startTime,
          endTime: this.endTime,
        })
          .then((res) => {
            console.log(res);
            window.location.href = "/exammanage";
          })
          .catch((err) => {
            console.log(err);
          });
      }
    },
  },
  created() {
    this.className = this.$route.params.className;
    this.classId = this.$route.params.classId;
  },
  mounted() {
    GetExamCreated().then((res) => {
      this.problemSetData = res.data.data;
    });
  },
  computed: {
    startTime() {
      var month = this.time[0].getMonth() + 1;
      return (
        this.time[0].getFullYear() +
        "-" +
        month +
        "-" +
        this.time[0].getDate() +
        " " +
        this.time[0].getHours() +
        ":" +
        this.time[0].getHours() +
        ":" +
        this.time[0].getSeconds()
      );
    },
    endTime() {
      var month = this.time[1].getMonth() + 1;
      return (
        this.time[1].getFullYear() +
        "-" +
        month +
        "-" +
        this.time[1].getDate() +
        " " +
        this.time[1].getHours() +
        ":" +
        this.time[1].getHours() +
        ":" +
        this.time[1].getSeconds()
      );
    },
  },
};
</script>

<style>
#examPublishRoot {
  position: relative;
      height: 753px;
}
#publishExamForm {
  position: absolute;
  width: 90%;
  left: 5%;
  top: 55px;
  border: 1px solid gray;
  background-color: white;
}
#publishExamBox {
  margin-top: 5px;
  margin-left: 15px;
  margin-bottom: 20px;
}
</style>